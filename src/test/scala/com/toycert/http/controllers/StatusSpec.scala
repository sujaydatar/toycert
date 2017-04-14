package com.toycert.http.controllers

import com.mashape.unirest.http.Unirest
import org.scalatest.DoNotDiscover

@DoNotDiscover
class StatusSpec extends RestSpec {
  "status endpoint" should "return success response" in {
    val uri = s"$baseUri/status"

    Unirest.get(uri).asString().getBody should be("OK")

  }
}
