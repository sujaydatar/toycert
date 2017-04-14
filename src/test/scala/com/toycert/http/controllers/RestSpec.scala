package com.toycert.http.controllers

import org.scalatest.{FlatSpec, Matchers}

trait RestSpec extends FlatSpec with Matchers {
  implicit lazy val dbm = RestTestSuite.dbManager
  val port = RestTestSuite.restPort
  implicit val baseUri = s"http://localhost:$port/api/v1"
}
