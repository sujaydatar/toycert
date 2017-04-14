package com.toycert.http.controllers

import java.io.InputStream
import java.security.cert.{CertificateFactory, X509Certificate}
import javax.naming.ldap.LdapName
import javax.security.auth.x500.X500Principal
import javax.ws.rs.core.{MediaType, Response}

import com.mashape.unirest.http.Unirest
import org.scalatest.DoNotDiscover
import scala.collection.JavaConverters._
import com.toycert.http.Protocol._

import TestHelpers._
@DoNotDiscover
class DownloadsControllerSpec extends  RestSpec {
  "downloaded pem file" should "have correct fields" in {
    val customerId = createAndVerifyCustomer("P","p@m.com","foobar")
    val ccr = CreateCertificateRequest("education.foo.com", "Acme, Inc", "USA", "CA", "SFO")

    val certificateId = createAndVerifyCertificate(customerId,
      ccr.commonName,
      ccr.organization,
      ccr.country,
      ccr.state,
      ccr.location)

    val downloadResponse = Unirest.get(
      s"$baseUri/downloads/certificates/$certificateId").header("Accept", MediaType.APPLICATION_OCTET_STREAM)
      .asObject(classOf[InputStream])

    downloadResponse.getStatus should equal(Response.Status.OK.getStatusCode)

    getValuesFromCert(downloadResponse.getBody) should equal(ccr)
  }

  def getValuesFromCert(is : InputStream) : CreateCertificateRequest = {
    val fact = CertificateFactory.getInstance("X.509");
    val x509Cert = fact.generateCertificate(is).asInstanceOf[X509Certificate]
    val map : Map[String, String] = x509Cert.getSubjectX500Principal.toMap()
    CreateCertificateRequest(
      map.get("CN").get,
      map.get("O").get,
      map.get("C").get,
      map.get("ST").get,
      map.get("L").get)
  }


  implicit class PrincipalExt(principal : X500Principal) {
    def toMap() : Map[String, String] = {
      val ln = new LdapName(principal.toString)
      ln.getRdns.asScala.toList.foldLeft(Map[String, String]())((acc,v) => {
        acc + (v.getType -> v.getValue.asInstanceOf[String])
      })
    }
  }
}
