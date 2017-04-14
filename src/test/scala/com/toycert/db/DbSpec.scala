package com.toycert.db

import org.scalatest.{FlatSpec, Matchers}

trait DbSpec extends FlatSpec with Matchers {
  implicit lazy val dbm = DbTestSuite.dbManager
  implicit lazy val bs = DbTestSuite.blobStore
}


