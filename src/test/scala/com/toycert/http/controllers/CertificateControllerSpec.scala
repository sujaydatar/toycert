package com.toycert.http.controllers

import java.net.InetSocketAddress
import java.util.concurrent.{CountDownLatch, TimeUnit}
import javax.ws.rs.core.Response
import com.mashape.unirest.http.Unirest
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import com.toycert.http.controllers.TestHelpers._
import com.toycert.http.HttpUtils.JsonUtil._
import com.toycert.http.Protocol._
import org.scalatest.DoNotDiscover
import scala.util.Random
@DoNotDiscover
class CertificateControllerSpec extends RestSpec {
  "certificate" should "be created" in {
    val customerId = createAndVerifyCustomer("John Doe","john@ccs1.com","foobar")
    val certificateId = createAndVerifyCertificate(customerId, "accounting.foo.com", "Acme, Inc", "USA", "CA", "SFO")
    certificateId should be > 0
  }

  "certificate" should "not be created if customer does not exist" in {
    val createCertificateRequest = CreateCertificateRequest("accounting.foo.com", "Acme, Inc", "USA", "CA", "SFO")
    val createCertificateRequestBody = serialize(createCertificateRequest)
    val response = Unirest.post(s"$baseUri/customers/${100 + Random.nextInt(10)}/certificates/")
      .header("Content-Type", "application/json")
      .body(createCertificateRequestBody.get).asString()
    response.getStatus  should equal (404)
  }

  "certificate with existing customer-id and common-name" should "be rejected" in {
    val customerId = createAndVerifyCustomer("Pat Doe","pat@ccs1.com","foobar")
    val certificateId = createAndVerifyCertificate(customerId, "restaurant.foo.com", "Acme, Inc", "USA", "CA", "SFO")
    val createCertificateRequest = CreateCertificateRequest("restaurant.foo.com", "Acme, Inc", "USA", "CA", "SFO")
    val createCertificateRequestBody = serialize(createCertificateRequest)
    val response = Unirest.post(s"$baseUri/customers/$customerId/certificates/")
      .header("Content-Type", "application/json")
      .body(createCertificateRequestBody.get).asString()

    response.getStatus  should equal (422)
  }

  "certificates state" should "be updated" in {
    val createCustomerRequest = CreateCustomerRequest("Don Doe", "don@ccs1.com", "foobar")
    val createCustomerRequestBody = serialize(createCustomerRequest)
    val createResp = Unirest.post(s"$baseUri/customers")
      .header("Content-Type", "application/json")
      .body(createCustomerRequestBody.get).asString()

    createResp.getStatus should equal(201)

    val createCustomerResponse = deserialize[CreateCustomerResponse](createResp.getBody)

    val createCertificateRequest = CreateCertificateRequest("accounting.foo.com", "Acme, Inc", "USA", "CA", "SFO")
    val createCertificateRequestBody = serialize(createCertificateRequest)
    val response = Unirest.post(s"$baseUri/customers/${createCustomerResponse.get.id}/certificates/")
        .header("Content-Type", "application/json").body(createCertificateRequestBody.get).asString()

    response.getStatus  should equal (201)

    val certificateId = deserialize[CreateCertificateResponse](response.getBody).get.id

    val server = HttpServer.create(new InetSocketAddress(9001), 0)
    val signal = new CountDownLatch(1)
    server.createContext("/callback", new CallbackHandler(signal))
    server.setExecutor(null)
    server.start()

    {
      val changeCertStReq = ChangeCertificateStateRequest(false, "http://localhost:9001/callback")

      val changeCertReqBody = serialize(changeCertStReq)

      Unirest.put(s"$baseUri/customers/${createCustomerResponse.get.id}/certificates/${certificateId}")
        .header("Content-Type", "application/json").body(changeCertReqBody.get).asString()

      val getResp = Unirest.get(s"$baseUri/customers/${createCustomerResponse.get.id}/certificates?active=true")
        .header("Accept", "application/json").asString()

      getResp.getStatus should equal(Response.Status.OK.getStatusCode)

      val validList = deserialize[GetCertByStateResponse](getResp.getBody).get.certificates

      validList.isEmpty should equal(true)
    }

    {
      val changeCertStReq = ChangeCertificateStateRequest(true, "http://locahost:9001/callback")

      val changeCertReqBody = serialize(changeCertStReq)

      Unirest.put(s"$baseUri/customers/${createCustomerResponse.get.id}/certificates/${certificateId}")
        .header("Content-Type", "application/json").body(changeCertReqBody.get).asString()

      val getResp = Unirest.get(s"$baseUri/customers/${createCustomerResponse.get.id}/certificates?active=true")
        .header("Accept", "application/json").asString()

      val validList = deserialize[GetCertByStateResponse](getResp.getBody).get.certificates

      validList should equal(List(createCertificateRequest.commonName))
    }

    signal.await(10, TimeUnit.SECONDS) should equal(true)
  }

  class CallbackHandler(cd : CountDownLatch) extends HttpHandler {
    override def handle(httpExchange: HttpExchange): Unit = {
      cd.countDown()
    }
  }
}
