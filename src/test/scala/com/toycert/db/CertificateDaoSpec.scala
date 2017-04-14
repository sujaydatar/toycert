package com.toycert.db

import com.toycert.Utils._
import com.toycert.cert.CertificateGenerator._
import com.toycert.db.dao.CertificateDao._
import com.toycert.db.dao.CustomerDao._
import com.toycert.db.model.Defs._
import org.scalatest.DoNotDiscover

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

@DoNotDiscover
class CertificateDaoSpec extends DbSpec {
  def customerList(email : String) = Seq(
    Customer("Some Doe", email, HashedPassword("foobar"))
  )

  def certificateRequestList(email : String) = Seq(
    CertificateData(email, "accounting.domain.com", "USA", "CA", "SFO", "Accounting", now(), plus(30)),
    CertificateData(email, "finance.domain.com", "USA", "CA", "SFO", "Finance", now(), plus(30)) ,
    CertificateData(email, "legal.domain.com", "USA", "CA", "SFO", "Legal", now(), plus(30)),
    CertificateData(email, "it.domain.com", "USA", "CA", "SFO", "IT", now(), plus(30))
  )

  def certificateRequestData(email : String) =
    CertificateData(email, "sales.domain.com",  "USA", "CA", "SFO", "IT", now(), plus(30))

  def createCustomers(list : Seq[Customer]) : Seq[Int] = list.map(c => createCustomerNonIdempotent(c).get)

  def createCustomer(customer : Customer) = createCustomerNonIdempotent(customer)

  def createCertificates(id : Int, list : Seq[CertificateData]) : Seq[Int] = {
    list.map( req => {

      (req, buildCertificate(req))

    }).map( tup => {

      val uri = Await.result(bs.store(tup._2._1), 10.seconds)
      CertificateWithCutomerId(id,tup._1.commonName, tup._2._2, uri)
    }).map({  certificate =>
      createCertificate(certificate).get
    })
  }

  "certificate" should "be created" in {
    val email = "john@doe.com"
    val customer = Customer("Some Doe", email, HashedPassword("foobar"))
    val id = createCustomerNonIdempotent(customer).get

    val certList = certificateRequestList(email)

    val ids = createCertificates(id, certList)

    val certs = ids.map(id => getCertificateById(id).get)

    certList.sortBy(_.commonName).zip(certs.sortBy(_.commonName)).foreach(tup => {
      tup._1.commonName should equal(tup._2.commonName)
    })
  }

  "certificate state" should "be updated" in {
    val email = "jane@doe.com"
    val customer= Customer("Jane Doe", email, HashedPassword("foobar"))

    val certList = certificateRequestList(email)
    val id = createCustomerNonIdempotent(customer).get

    createCertificates(id, certList).map(certId => {
      updateState(certId, false)
      certId
    }).map(certId => {
      getCertificateById(certId).get.active should equal(false)
      certId
    }).map(certId => {
      updateState(certId, true)
      certId
    }).map(certId => {
      getCertificateById(certId).get.active should equal(true)
    })
  }

  "certificates" should "be deleted by id" in {
    val email = "kayne@doe.com"
    val customer= Customer("Kayne Doe", email, HashedPassword("foobar"))
    val certList = certificateRequestList(email)
    val id = createCustomerNonIdempotent(customer).get
    val ids = createCertificates(id, certList)
    ids.map(id => {
      deleteCertificate(id)
      id
    }).map(id => {
      getCertificateById(id).isFailure should equal(true)
    })
  }

  "certificates with existing customer-id and common_name" should "be rejected" in {
    val email = "zoe@doe.com"
    val customer= Customer("Zoe Doe", email, HashedPassword("foobar"))
    val certData = certificateRequestData(email)
    val customerId = createCustomerNonIdempotent(customer).get

    val (certBody, privateKey) = buildCertificate(certData)
    val uri = Await.result(bs.store(certBody), 10.seconds)
    val certificateId = createCertificate(
      CertificateWithCutomerId(customerId,certData.commonName, privateKey, uri, true))

    val (certBody1, privateKey1) = buildCertificate(certData)
    val uri1 = Await.result(bs.store(certBody1), 10.seconds)

    createCertificate(
      CertificateWithCutomerId(customerId,certData.commonName, privateKey1, uri1, true)
    ).isFailure should equal(true)
  }

  "only active certificates" should "be retrieved" in {
    val email = "zack@doe.com"
    val customer= Customer("Zack Doe", email, HashedPassword("foobar"))
    val certList = certificateRequestList(email)
    val customerId = createCustomerNonIdempotent(customer).get
    val certificateIds = createCertificates(customerId, certList)

    val (activeList, inactiveList) = certificateIds.foldLeft((Seq[Int](),Seq[Int]()))((acc, v)  => {
      Random.nextBoolean() match {
        case true =>
          updateState(v, false)
          (acc._1, acc._2 :+ v)
        case false =>
          (acc._1 :+ v, acc._2)
      }
    })

    {
      val retrievedCerts = getCertificatesForCustomerByIdAndState(customerId, true).get.get
      retrievedCerts.sortBy(_.id).zip(activeList.sorted).foreach(tup => tup._1.id should equal(tup._2))
    }

    {
      val retrievedCerts = getCertificatesForCustomerByIdAndState(customerId, false).get.get
      retrievedCerts.sortBy(_.id).zip(inactiveList.sorted).foreach(tup => tup._1.id should equal(tup._2))
    }
  }
}
