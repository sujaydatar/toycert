package com.toycert

import java.io.{PrintWriter, StringWriter}
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.{Date, Optional}

import com.toycert.config.Defs.MySQLConfig
import com.toycert.db.DbUtils.DbConfig

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object Utils {
  implicit val executors =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

  implicit class any2try[T](expression: => T) {
    def toTry = Try(expression)
  }

  implicit class mysql2db(mysql : MySQLConfig) {
    def toDbConfig = {
      val url = s"jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.db}?createDatabaseIfNotExist=true"
      DbConfig(mysql.user, mysql.password, url)
    }
  }

  implicit class throwable2string(t : Throwable) {
    def asString() : String = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      val body = t.printStackTrace(pw)
      pw.flush()
      pw.close()
      sw.toString
    }
  }

  implicit class jOptional2SOption[T](javaOptional : Optional[T]) {
    def toOption() = if(javaOptional.isPresent) Option(javaOptional.get()) else None
  }

  implicit class leftMappableTry[T](data : Try[T]) {
    def leftMap[U <: Throwable](map : Throwable => U) : Try[T]= data match {
      case Success(_) => data
      case Failure(t) => Try {
        throw map(t)
      }
    }
  }

  def now() = new Date()

  def plus(days : Long) = Date.from(Instant.now().plus(30, ChronoUnit.DAYS))

  object Exceptions {

    final case class EntityExistsException(message : String) extends Exception(message)

    final case class EntityNotFoundException(message : String) extends Exception(message)

  }
}
