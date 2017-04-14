package com.toycert.http.controllers

import javax.ws.rs.core.Response

import com.mashape.unirest.http.Unirest
import com.toycert.http.HttpUtils.JsonUtil._
import com.toycert.http.Protocol._
import org.scalatest.{DoNotDiscover, Matchers}

@DoNotDiscover
object TestHelpers extends Matchers {
  case class TestResponse(statusCode : Int, body : String)

  def createCustomerResponse(name : String,
                             email : String,
                             password : String
                            )(implicit baseUri : String) : TestResponse = {
    val createCustomerRequest = CreateCustomerRequest(name, email, password)

    val createCustomerRequestBody = serialize(createCustomerRequest)

    val createResp = Unirest.post(s"$baseUri/customers").header("Content-Type",
      "application/json").body(createCustomerRequestBody.get).asString()

    TestResponse(createResp.getStatus, createResp.getBody)
  }

  def createCertificateResponse(customerId : Int,
                                commonName : String,
                                organization : String,
                                country : String,
                                state : String,
                                location : String)(implicit baseUri : String) : TestResponse = {
    val createCertificateRequest = CreateCertificateRequest(commonName, organization, country, state, location)
    val createCertificateRequestBody = serialize(createCertificateRequest)
    val response = Unirest.post(s"$baseUri/customers/$customerId/certificates").header("Content-Type", "application/json")
      .body(createCertificateRequestBody.get).asString()
    TestResponse(response.getStatus, response.getBody)
  }


  def createAndVerifyCustomer(name : String, email : String, password : String)(implicit baseUri : String) : Int = {
    val TestResponse(status, body) = createCustomerResponse(name, email, password)
    status should equal(Response.Status.CREATED.getStatusCode)
    deserialize[CreateCustomerResponse](body).get.id
  }

  def createAndVerifyCertificate(customerId : Int,
                                 commonName : String,
                                 organization : String,
                                 country : String,
                                 state : String,
                                 location : String)(implicit baseUri : String) : Int = {

    val TestResponse(status, body) = createCertificateResponse(customerId,
      commonName, organization, country, state, location)
    status should equal(Response.Status.CREATED.getStatusCode)
    deserialize[CreateCertificateResponse](body).get.id
  }

}
