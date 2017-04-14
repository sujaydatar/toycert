package com.toycert.http.controllers

import java.net.URI
import javax.ws.rs.{Produces, _}
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.{MediaType, Response}
import com.toycert.Utils._
import com.toycert.blobstore.Defaults._
import com.toycert.cert.CertificateGenerator._
import com.toycert.db.dao.CertificateDao._
import com.toycert.db.dao.CustomerDao._
import com.toycert.db.DbUtils._
import com.toycert.db.DbUtils.Defaults._
import com.toycert.db.model.Defs._
import com.toycert.http.HttpUtils.Notifier._
import com.toycert.http.Protocol._
import org.slf4j.LoggerFactory
import com.toycert.http.HttpUtils.JsonUtil._
import com.toycert.http.HttpUtils.RestUtils._
import scala.util.{Failure, Success, Try}

@Path("/api/v1/customers/{customerId}/certificates")
class CertificatesController {
  private val logger = LoggerFactory.getLogger(classOf[CertificatesController])

  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def createCertificateHTTP(@Suspended response : AsyncResponse,
                            @PathParam("customerId")custId : Int,
                            body : String) : Unit = {
    logger.debug(s"Received create certificate request for customer with id=$custId")

    implicit val dbManager = getDbManager().get
    implicit val blobStore = getBlobStore().get
    val partialResult =
      (for {
           ccr <- deserialize[CreateCertificateRequest](body)

      customer <- getCustomerById(custId)

     certData  <- CertificateData(
                      customer.email,
                      ccr.commonName,
                      ccr.country,
                      ccr.state,
                      ccr.location,
                      ccr.organization, now, plus(30)).toTry

     (pem, pk) <- buildCertificate(certData).toTry

     tryFuture <- blobStore.store(pem).toTry

    } yield {
      (ccr, pk, tryFuture)
    })

    partialResult match {
      case Success((certReq, privateKey, future)) =>
        future onComplete {
          case Success(uri) =>
            (for {

              certificateId <- saveCertificateEntity(custId, privateKey, certReq, uri)

                        ccr <- CreateCertificateResponse(certificateId).toTry

               responseBody <- serialize(ccr)

            } yield {
              logger.debug(s"Created certificate with id=$certificateId")
              responseBody
            }) match {
              case Success(body) =>
                response.resume(getCreatedResponse(body))
              case Failure(t) =>
                logger.error(s"Error creating certificate for customer with id=$custId")
                response.resume(getResponseFromException(t))
            }

          case Failure(t) =>
            logger.error(s"Error creating certificate for customer with id=$custId", t)
            response.resume(getInternalErrorResponse("unable to store generated .pem certificate"))
        }
      case Failure(t) =>
        response.resume(getResponseFromException(t))
    }
  }

  @PUT @Path("/{certificateId}")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  def changeState(@PathParam("customerId")customerId : Int,
                  @PathParam("certificateId")certificateId : Int,
                  body : String) : Response = {
    logger.debug(s"Received put request for certificate with id : $certificateId")

    implicit val dbManager = getDbManager().get
    implicit val blobStore = getBlobStore().get

    (for {

              custId  <- getCustomerById(customerId)

              certId  <- getCertificateById(certificateId)

       changeStateReq <- deserialize[ChangeCertificateStateRequest](body)

                    _ <- updateState(certId.id, changeStateReq.active).toTry

    } yield {

      (changeStateReq, getOkResponse())

     }
    ).flatMap(r => Try {

      serialize(CertificateStateChangedEvent(s"state changed to ${r._1.active}", certificateId)) match {
        case Success(b) => postNotification(r._1.callbackUrl, b).onComplete {
          case _ => ()
        }
        case Failure(t) =>
          logger.error(s"error posting state change event to notification url : ${r._1.callbackUrl}")
      }
      r._2

    }).asResponse()
  }

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCertificatesByState(@PathParam("customerId")customerId : Int,
                              @QueryParam("active")active : Boolean): Response = {

    implicit val dbManager = getDbManager().get
    implicit val blobStore = getBlobStore().get

    logger.debug(s"received get request for active certificates for customer id : ${customerId}, active : $active")
    (for {

          customer  <- getCustomerById(customerId)

      certificates  <- getCertificatesForCustomerByIdAndState(customerId, active)

       commonNames  <- certificates.map(l => l.map(cwci => cwci.commonName)).toTry

        bodyString  <- serialize(GetCertByStateResponse(commonNames.getOrElse(List())))

    } yield {
      getOkResponse(bodyString)
    }).asResponse()
  }

  @GET @Path("/{certificateId}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCertificateForId(@PathParam("customerId")customerId : Int,
                          @PathParam("certificateId") certificateId : Int) : Response =  {
    implicit val dbManager = getDbManager().get
    implicit val blobStore = getBlobStore().get

    logger.debug(s"received get request for active certificates for" +
      s" customer id : ${customerId}, certificate id : $certificateId")

    (for {

      customer  <- getCustomerById(customerId)

      certificate  <- getCertificateById(certificateId)

      bodyString  <- serialize(LightCertificate(certificate.commonName))

    } yield {
      getOkResponse(bodyString)
    }).asResponse()
  }

  def saveCertificateEntity(customerId :Int,
                            privateKey : Array[Byte],
                            ccr : CreateCertificateRequest,
                            fileUri : URI)(implicit  dbManager : DbManager) = {
    createCertificate(CertificateWithCutomerId(customerId, ccr.commonName, privateKey, fileUri))
  }
}
