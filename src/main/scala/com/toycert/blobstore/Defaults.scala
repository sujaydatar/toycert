package com.toycert.blobstore

import java.util.concurrent.atomic.AtomicReference

object Defaults {
  val blobStoreRef : AtomicReference[BlobStore] = new AtomicReference[BlobStore]()


  def initBlobStore(dbm: BlobStore): Unit = synchronized {
    if(blobStoreRef.get == null)
      Defaults.blobStoreRef.set(dbm)
  }

  def getBlobStore()  = Option(blobStoreRef.get())
}