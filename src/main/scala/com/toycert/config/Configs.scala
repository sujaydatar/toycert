package com.toycert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.{ClassTag, classTag}
import scala.util.Try

object Defs {
  case class MySQLConfig(user : String, password : String, val host : String, val port : Int, val db : String) {
    override def toString = s"MySQLConfig(user=$user,host=$host,port=$port,db=$db)"
  }
  case class HttpConfig(host: String , port : Int)
  case class ServiceConfig(mysql : MySQLConfig, http : HttpConfig, certsDir : String)
}

object ConfigParser {
  val mapper : ObjectMapper = {
    val m = new ObjectMapper(new YAMLFactory())
    m.registerModule(DefaultScalaModule)
    m
  }

  def parse[T : ClassTag](yaml : String) : Try[T]= {
    Try(mapper.readValue(yaml, classTag[T].runtimeClass).asInstanceOf[T])
  }
}



