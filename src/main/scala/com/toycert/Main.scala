package com.toycert

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import com.toycert.Utils._
import com.toycert.blobstore.Defaults._
import com.toycert.blobstore.LocalFileSystemBlobStore
import com.toycert.config.ConfigParser._
import com.toycert.config.Defs.ServiceConfig
import com.toycert.http.HttpServer._
import org.skife.jdbi.v2.DBI
import org.slf4j.LoggerFactory
import com.toycert.db.DbUtils.Defaults._
import com.toycert.db.DbUtils.DbConfig._
import com.toycert.db.DbUtils.DbManager
import com.toycert.db.DbUtils.Migrator._
import scala.util.{Failure, Success}
import Utils._
object Main {
  def main(args : Array[String]) {
    val logger = LoggerFactory.getLogger(Main.getClass);

    logger.debug(s"Starting service")

    (for {
       configFileBytes  <- Files.readAllBytes(Paths.get("config.yml")).toTry

       configFileString <- new String(configFileBytes, StandardCharsets.UTF_8).toTry

                 config <- parse[ServiceConfig](configFileString)

                      _ = logger.debug(s"Parsed service configuration = $config")

               dbConfig <- config.mysql.toDbConfig.toTry

                      _ <- migrate("classpath:migration", dbConfig)

              dbManager <- DbManager(new DBI(createDataSource(dbConfig))).toTry

                     _  <- initDb(dbManager).toTry

              blobStore <- new LocalFileSystemBlobStore(Paths.get(config.certsDir)).toTry

                      _ <- initBlobStore(blobStore).toTry

      startServerFuture <- start(config.http, dbManager, blobStore).toTry

    } yield {
      (startServerFuture, config)
    }).flatMap(tuple => {
      tuple._1.onComplete {
          case Success(server) =>
            logger.debug("stopping http server")
            server.destroy();

          case Failure(t) =>
            logger.error("http service interrupted, exiting")
            System.exit(0)
        }
      tuple._2.toTry
    }) match {
      case Success(c) =>
        logger.debug(s" started http server on port : ${c.http.port}")

      case Failure(t) =>
        logger.error("Exception starting server, exiting", t)
        System.exit(0)
    }
  }
}

