package com.toycert.http.controllers


import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.toycert.blobstore.LocalFileSystemBlobStore
import com.toycert.config.Defs.HttpConfig
import com.toycert.db.DbUtils._
import DbConfig._
import Migrator._
import com.toycert.http.HttpServer._
import org.apache.commons.io.FileUtils
import org.scalatest._
import org.skife.jdbi.v2.DBI

import scala.util.Random

class RestTestSuite extends Suites(
  new StatusSpec,
  new CustomersControllerSpec,
  new CertificateControllerSpec,
  new DownloadsControllerSpec) with BeforeAndAfterAll {

  import RestTestSuite._

  override def beforeAll() = {
    dropDatabase(DbConfig(user, pass, url))
    migrate("classpath:migration",DbConfig(user, pass, url)).get
    startServer()
    ()
  }

  override def afterAll() = {
    dropDatabase(DbConfig(user, pass, url))
    FileUtils.deleteQuietly(new File(blobStoreDir))
  }

  def dropDatabase(dc : DbConfig): Unit = {
    val dc = DbConfig(user, pass, url)
    val dbi = new DBI(createDataSource(dc))
    val handle = dbi.open()
    handle.execute(s"DROP DATABASE IF EXISTS $testDatabaseName")
    handle.close()
  }

  def startServer() = {
    val cdl = new CountDownLatch(1)
    start(HttpConfig("localhost", restPort), dbManager, blobStore, cdl)
    cdl.await(10, TimeUnit.SECONDS)
  }
}

object RestTestSuite {
  val restPort = 30000 + Random.nextInt(30000)
  val testDatabaseName = "toycerttest_rest"
  val host = "localhost"
  val port = 3306
  val db = "mysql"
  val url = s"jdbc:$db://$host:$port/$testDatabaseName?createDatabaseIfNotExist=true"
  val user = "testuser"
  val pass = "testuser"
  val blobStoreDir = "blob_rest_test_suite"
  val blobStore =  {
    FileUtils.deleteQuietly(new File(blobStoreDir))
    Files.createDirectory(Paths.get(blobStoreDir))
    new LocalFileSystemBlobStore(Paths.get(blobStoreDir))
  }
  val dbConfig = DbConfig(user, pass, url)

  val dbManager : DbManager = {
    DbManager(new DBI(createDataSource(dbConfig)))
  }
}
