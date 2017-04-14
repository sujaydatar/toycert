package com.toycert.db.dao

import java.net.URI
import java.sql.ResultSet

import com.toycert.Utils.Exceptions._
import com.toycert.db.DbUtils._
import com.toycert.db.DbUtils.DaoFactory._
import com.toycert.db.model.Defs._
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException
import org.skife.jdbi.v2.sqlobject.customizers.Mapper
import org.skife.jdbi.v2.sqlobject.{Bind, GetGeneratedKeys, SqlQuery, SqlUpdate}
import org.skife.jdbi.v2.tweak.ResultSetMapper
import org.slf4j.LoggerFactory
import com.toycert.Utils._

import scala.collection.JavaConverters._
import scala.util.Try

trait CertificateDao {
  @SqlUpdate("INSERT INTO certificates (common_name, private_key, customer_id, content_link) values(:commonName, :privateKey, :customerId, :contentLink)")
  @GetGeneratedKeys
  def createCertificateWithId(@Bind("commonName") commonName : String,
                              @Bind("privateKey") privateKey : Array[Byte],
                              @Bind("customerId") customerId : Int,
                              @Bind("contentLink") contentLink : String) : Int

  @SqlUpdate("DELETE FROM certificates WHERE customer_id=:ci")
  def deleteCertificate(@Bind("ci") customerId : Int) : Unit

  @SqlUpdate("UPDATE certificates SET active=:active where id=:id")
  def updateState(@Bind("id") id : Int, @Bind("active") active : java.lang.Byte)

  @SqlQuery("SELECT * FROM certificates WHERE customer_id=:customerId AND active=:active")
  @Mapper(classOf[CertificateWithIdMapper])
  def getCertificatesForCustomerByIdAndState(@Bind("customerId") customerId : Int,
                                     @Bind("active") active : java.lang.Byte) : java.util.List[CertificateWithCutomerId]

  @SqlQuery("SELECT * FROM certificates WHERE id=:id")
  @Mapper(classOf[CertificateWithIdMapper])
  def getCertificateById(@Bind("id") id : Int) : CertificateWithCutomerId
}

object CertificateDao {
  def createCertificate(certificate : CertificateWithCutomerId)(implicit dbm : DbManager) : Try[Int] = Try {
    dbm.createCertificateWithId(
      certificate.commonName,
      certificate.privateKey,
      certificate.id,
      certificate.certificateLink.toString)
  }.leftMap(t => t match {
    case scve : UnableToExecuteStatementException =>
      new EntityExistsException(s"customer " +
        s"with email=${certificate.commonName} already exists for customer id : ${certificate.id}")
    case t => t
  })

  def getCertificateById(id : Int)(implicit dbm : DbManager) : Try[CertificateWithCutomerId] = Try {
    val result = dbm.getCertificateById(id)
    if(result == null)
      throw new EntityNotFoundException(s"Certificate with id=$id does not exist")
    result
  }

  def updateState(id : Int, active : Boolean)(implicit dbm : DbManager) : Unit = {
    dbm.updateState(id, active.asJavaByte)
  }

  def deleteCertificate(id : Int)(implicit dbm : DbManager) : Unit = {
    dbm.deleteCertificate(id)
  }

  def getCertificatesForCustomerByIdAndState(customerId : Int, active : Boolean)
                                            (implicit dbm : DbManager) : Try[Option[List[CertificateWithCutomerId]]] = Try {
    val result = dbm.getCertificatesForCustomerByIdAndState(customerId, active.asJavaByte)
    if(result == null) None else Some(result.asScala.toList)
  }

  implicit class bool2str(b : Boolean) {
    def asJavaByte = java.lang.Byte.valueOf(if(b == true) "1" else "0")
  }
}

class CertificateWithIdMapper extends ResultSetMapper[CertificateWithCutomerId]{
  val logger = LoggerFactory.getLogger(getClass)

  override def map(index: Int, r: ResultSet, ctx: StatementContext): CertificateWithCutomerId = {
    val id = r.getInt("id")
    val commonName : String = r.getString("common_name")
    val pk : Array[Byte] = r.getObject("private_key").asInstanceOf[Array[Byte]]
    val link : String = r.getString("content_link")
    val active : Boolean = r.getBoolean("active")
    val retval = CertificateWithCutomerId(id, commonName, pk, new URI(link), active)
    retval
  }
}
