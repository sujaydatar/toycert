package com.toycert.http.controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.ws.rs.container.{AsyncResponse, Suspended}
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, PathParam, Produces}

import com.toycert.Utils._
import com.toycert.blobstore.Defaults._
import com.toycert.db.dao.CertificateDao._
import org.slf4j.LoggerFactory
import com.toycert.http.HttpUtils.RestUtils._
import com.toycert.db.DbUtils.Defaults._
import scala.util.{Failure, Success}

@Path("/api/v1/downloads/certificates/{certificateId}")
class DownloadsController {
  val logger = LoggerFactory.getLogger(getClass)

  @GET
  @Produces(Array(MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN))
  def downloadCertificate(@PathParam("certificateId") certificateId : Int,
                          @Suspended asyncResponse : AsyncResponse): Unit = {
    implicit val asr = asyncResponse
    implicit val dbm = getDbManager().get
    val blobstore = getBlobStore().get

    (for {
        certificate <- getCertificateById(certificateId)

             future <- blobstore.get(certificate.certificateLink).toTry
    } yield {
      (certificate, future)
    }) match {
      case Success((certificate, future)) => future onComplete {
        case Success(bytes) =>
          val filename =  s"certificate_${certificate.id}_${getTimestamp()}.pem"
          logger.debug(s"Sending file $filename for certificate id : $certificateId")
          getFileDownloadResponse(bytes,filename).complete()
        case Failure(t) =>
          logger.error(s"Unable to read certificate from ${certificate.certificateLink}", t)
          getResponseFromException(t).complete()
      }

      case Failure(t) =>
        logger.error(s"Error locating certificate for certificate id : $certificateId", t)
        getResponseFromException(t).complete()
    }
  }

  def getTimestamp() : String = {
    new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
  }

  implicit class completer(r : Response) {
    def complete()(implicit asyncResponse : AsyncResponse) : Unit = {
      asyncResponse.resume(r)
    }
  }
}
