package com.toycert.db

import com.toycert.db.dao.TestDao._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class QuerySpec extends DbSpec {
  "test query" should "return 1" in {
    test() should equal (1)
  }
}
