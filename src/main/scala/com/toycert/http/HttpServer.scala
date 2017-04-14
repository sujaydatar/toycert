package com.toycert.http

import java.util.concurrent.CountDownLatch
import com.toycert.Utils._
import com.toycert.blobstore.BlobStore
import com.toycert.blobstore.Defaults._
import com.toycert.config.Defs.HttpConfig
import com.toycert.db.DbUtils.DbManager
import com.toycert.db.DbUtils.Defaults._
import com.toycert.http.controllers.{CertificatesController, CustomersController, DownloadsController, StatusController}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import scala.concurrent.Future

object HttpServer {
  def start(httpConf : HttpConfig,
            dbManager : DbManager,
            blobStore : BlobStore,
            startedSignal : CountDownLatch = new CountDownLatch(1)) = Future {

    initDb(dbManager)

    initBlobStore(blobStore)

    val config = new ResourceConfig()

    config.registerClasses(
      classOf[StatusController],
      classOf[CertificatesController],
      classOf[CustomersController],
      classOf[DownloadsController]
      )

    val servlet = new ServletHolder(new ServletContainer(config))
    val server = new Server(httpConf.port)
    val context = new ServletContextHandler(server, "/")
    context.addServlet(servlet, "/*")
    server.start()
    startedSignal.countDown();
    server.join()
    server
  }
}
