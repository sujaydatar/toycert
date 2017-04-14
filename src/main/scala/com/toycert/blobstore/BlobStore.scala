package com.toycert.blobstore

import java.net.URI

import scala.concurrent.Future

trait BlobStore {
  def store(string : String) : Future[URI]
  def get(url : URI) : Future[Array[Byte]]
}