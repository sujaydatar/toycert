package com.toycert.config

import com.toycert.config.Defs.ServiceConfig
import org.scalatest.{FlatSpec, Matchers}

class ConfigTest extends FlatSpec with Matchers {
  "config" should "be de-serialized" in {
    val user = "testuser"
    val password = user
    val host = "localhost"
    val port = 8080
    val db = "toycertdb"
    val certsDir = "toycert_store"

    val yaml : String = s"""
      |mysql:
      |  user: $user
      |  password: $password
      |  host: localhost
      |  port: 3306
      |  db:  $db
      |http:
      |  port: $port
      |  host: $host
      |certsDir: $certsDir
    """.stripMargin

    val sc = ConfigParser.parse[ServiceConfig](yaml).get
    assert( sc.mysql.user == user)
    assert( sc.mysql.password == password)
    assert( sc.http.port == port)
    assert( sc.http.host == host)
    assert(sc.mysql.db == db)
    assert( sc.certsDir == certsDir)
  }

}

