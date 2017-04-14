package com.toycert.http.controllers
import com.mashape.unirest.http.Unirest
import com.toycert.http.HttpUtils.JsonUtil._
import com.toycert.http.Protocol._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CustomersControllerSpec extends RestSpec {
  "customer" should "be created" in {
    val ccr = CreateCustomerRequest("John Doe", "john@grio.com", "foobar")
    val reqBody = serialize(ccr)
    val createResp = Unirest.post(s"$baseUri/customers").header("Content-Type", "application/json")
      .body(reqBody.get).asString()

    createResp.getStatus should equal(201)

    val createdId = deserialize[CreateCustomerResponse](createResp.getBody).get.id

    val getResponse = Unirest.get(s"$baseUri/customers/$createdId").asString()

    val lc = deserialize[LightCustomer](getResponse.getBody)

    lc.get.email should be(ccr.email)

    lc.get.name should be(ccr.name)
  }

  "post request to create customer with existing email" should "fail" in {
    val ccr1 = CreateCustomerRequest("John Doe", "john@foo2.com", "foobar")
    val reqBody1 = serialize(ccr1)

    val createResp1 = Unirest.post(s"$baseUri/customers").header("Content-Type", "application/json")
      .body(reqBody1.get).asString()

    createResp1.getStatus should equal(201)

    val ccr2 = CreateCustomerRequest("John Doe Jr", "john@foo2.com", "foobar")
    val reqBody2 = serialize(ccr2)

    val createResp2 = Unirest.post(s"$baseUri/customers").header("Content-Type", "application/json")
      .body(reqBody2.get).asString()

    createResp2.getStatus should equal(422)
  }

  "customer" should "be deleted" in {
    val ccr = CreateCustomerRequest("John Doe", "john@zoo.com", "foobar")
    val reqBody = serialize(ccr)
    val createResp = Unirest.post(s"$baseUri/customers").header("Content-Type", "application/json")
      .body(reqBody.get).asString()
    createResp.getStatus should equal(201)

    val createdId = deserialize[CreateCustomerResponse](createResp.getBody).get.id

    val deleteResp = Unirest.delete(s"$baseUri/customers/$createdId").asString()

    deleteResp.getStatus should equal(200)
  }
}
