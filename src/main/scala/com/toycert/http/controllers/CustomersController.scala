package com.toycert.http.controllers

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{Path, Produces, _}

import com.toycert.Utils._
import com.toycert.db.dao.CustomerDao._
import com.toycert.db.DbUtils.Defaults._
import com.toycert.db.model.Defs._
import com.toycert.http.HttpUtils.JsonUtil._
import com.toycert.http.Protocol._
import org.slf4j.LoggerFactory
import com.toycert.http.HttpUtils.RestUtils._

@Path("/api/v1/customers/")
class CustomersController {
  private val logger = LoggerFactory.getLogger(classOf[CustomersController])

  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def createCustomerHTTP(body : String) : Response  = {
    logger.debug(s"Received create customer request")

    implicit val db = getDbManager().get

    (for {

      ccreq <- deserialize[CreateCustomerRequest](body)

      id <- createCustomerNonIdempotent(ccreq)

      ccresp <- CreateCustomerResponse(id).toTry

      respBody <- serialize(ccresp)

    } yield {
      logger.debug(s"Created customer with $id")
      getCreatedResponse(respBody)

    }).asResponse()
  }

  @GET @Path("{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCustomerHTTP(@PathParam("id")id : Int) : Response = {
    logger.debug(s"Receive get request for customer $id")
    implicit val db = getDbManager().get

    (for {
        c <- getCustomerById(id)
        lc <- LightCustomer(id = c.id.get, name = c.name, email = c.email).toTry
        lcs <- serialize(lc)
      } yield {
        logger.debug(s"Got customer with $id")
        getOkResponse(lcs)
      }
    ).asResponse()
  }

  @DELETE @Path("{id}")
  def deleteCustomerHTTP(@PathParam("id")id : Int) : Response = {
    logger.debug(s"Received delete request for customer with id=$id")
    implicit val db = getDbManager().get

    (for {
      _ <- deleteCustomerById(id).toTry
    } yield {
      logger.debug(s"Deleted customer with id=$id")
      getOkResponse()
    }).asResponse()
  }

  implicit def ccr2cust(ccr : CreateCustomerRequest) : Customer =
    Customer(ccr.name, ccr.email, HashedPassword(ccr.password))
}
