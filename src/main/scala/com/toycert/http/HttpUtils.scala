package com.toycert.http

import javax.ws.rs.core.Response

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.mashape.unirest.http.Unirest
import com.toycert.Utils.Exceptions._
import com.toycert.http.Protocol._
import org.slf4j.LoggerFactory
import com.toycert.Utils._
import scala.concurrent.Future
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

object HttpUtils {
  object JsonUtil {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    def deserialize[T : ClassTag](json : String) : Try[T]= {
      Try(mapper.readValue(json, classTag[T].runtimeClass).asInstanceOf[T])
    }

    def serialize[T](data : T) : Try[String] = {
      Try(mapper.writeValueAsString(data)).map(_ + "\n")
    }
  }

  object RestUtils {
    import JsonUtil._
    private val logger = LoggerFactory.getLogger(this.getClass)

    def getCreatedResponse(body : String) : Response = {
      Response.status(Response.Status.CREATED).entity(body).build()
    }

    def getOkResponse(body : String) : Response = {
      Response.status(Response.Status.OK).entity(body).build()
    }

    def getOkResponse() : Response = {
      Response.status(Response.Status.OK).build()
    }

    def getFileDownloadResponse(bytes : Array[Byte], fileName : String) : Response = {
      val response = Response.ok(bytes);
      response.header("Content-Disposition", s"attachment;filename=${fileName}")
      response.build()
    }

    def getInternalErrorResponse(t : Throwable) : Response = {
      Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(t.toString).build()
    }

    def getInternalErrorResponse(msg : String) : Response = {
      Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build()
    }

    def getResponseFromException(t : Throwable) : Response = {
      t match {
        case enf : EntityNotFoundException =>
          getNotFoundResponse(new ErrorResponse(enf.getMessage))
        case eae : EntityExistsException =>
          getUnprocessableEntity(new ErrorResponse(eae.getMessage))
        case _ => getInternalErrorResponse(t)
      }
    }

    def getNotFoundResponse(error : ErrorResponse) : Response = {
      Response.status(Response.Status.NOT_FOUND).entity(serialize(error).get).build()
    }

    def getUnprocessableEntity(error : ErrorResponse) : Response = {
      Response.status(422).entity(serialize(error).get).build()
    }

    implicit class try2response(t : Try[Response]) {
      def asResponse() : Response = t match {
        case Success(r) => r
        case Failure(t) =>
          logger.error(s"Returning error response", t)
          getResponseFromException(t)
      }
    }
  }

  object Notifier {
    def postNotification(uri : String, body : String) = Future {
      Unirest.post(uri).body(body).asString()
    }
  }
}
