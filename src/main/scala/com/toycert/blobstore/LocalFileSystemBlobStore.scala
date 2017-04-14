package com.toycert.blobstore
import java.io.File
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import com.toycert.Utils._
import scala.concurrent.Future

class LocalFileSystemBlobStore(baseDirectory : Path) extends BlobStore {
  override def store(body: String): Future[URI] = Future {
    val newfileName = UUID.randomUUID() + ".pem"
    val newFilePath = getFilePath(newfileName)
    Files.write(newFilePath, body.getBytes)
    newFilePath.toUri
  }

  override def get(uri: URI): Future[Array[Byte]] = Future {
    Files.readAllBytes(Paths.get(uri))
  }

  def getFilePath(newFileName : String) : Path = {
    if(!Files.exists(baseDirectory))
      Files.createDirectories(baseDirectory)
    Paths.get(baseDirectory + File.separator + newFileName)
  }
}
