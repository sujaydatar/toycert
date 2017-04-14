package com.toycert.db

import java.io.File
import java.nio.file.{Files, Paths}

import com.toycert.blobstore.LocalFileSystemBlobStore
import com.toycert.db.DbUtils.{DbConfig, DbManager}
import com.toycert.db.DbUtils.Migrator._
import com.toycert.db.DbUtils.DbConfig._
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.skife.jdbi.v2.DBI

class DbTestSuite extends Suites(new QuerySpec, new CustomerDaoSpec, new CertificateDaoSpec) with BeforeAndAfterAll {
  import DbTestSuite._
  override def beforeAll(): Unit = {
    dropDatabase(DbConfig(user, pass, url))
    migrate("classpath:migration",DbConfig(user, pass, url)).get
  }
  override def afterAll() {
    dropDatabase(DbConfig(user, pass, url))
    deleteBlobStore()
  }

  def dropDatabase(dc : DbConfig): Unit = {
    val dc = DbConfig(user, pass, url)
    val dbi = new DBI(createDataSource(dc))
    val handle = dbi.open()
    handle.execute(s"DROP DATABASE IF EXISTS $testDatabaseName")
    handle.close()
  }
}

object DbTestSuite {
  val testDatabaseName = "toycerttest"
  val host = "localhost"
  val port = 3306
  val db = "mysql"
  val url = s"jdbc:$db://$host:$port/$testDatabaseName?createDatabaseIfNotExist=true"
  val user = "testuser"
  val pass = "testuser"
  val blobStoreDir = "blob_db_test_suite"

  val dbConfig = DbConfig(user, pass, url)

  val blobStore =  {
    FileUtils.deleteQuietly(new File(blobStoreDir))
    Files.createDirectory(Paths.get(blobStoreDir))
    new LocalFileSystemBlobStore(Paths.get(blobStoreDir))
  }

  val dbManager : DbManager = {
    DbManager(new DBI(createDataSource(dbConfig)))
  }

  def deleteBlobStore() : Unit = {
    FileUtils.deleteQuietly(new File(blobStoreDir))
  }
}
