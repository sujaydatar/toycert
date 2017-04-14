package com.toycert.db

import java.util.concurrent.atomic.AtomicReference
import javax.sql.DataSource
import com.toycert.db.dao.{CertificateDao, CustomerDao, TestDao}
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.skife.jdbi.v2.DBI

import scala.util.Try

object DbUtils {
  object Defaults {
    val dbManagerRef : AtomicReference[DbManager] = new AtomicReference[DbManager]()


    def initDb(dbm: DbManager): Unit = synchronized {
      if(dbManagerRef.get == null)
        Defaults.dbManagerRef.set(dbm)
    }

    def getDbManager()  = Option(dbManagerRef.get())
  }

  final case class DbManager(val dbi : DBI)

  object Migrator {
    import DbConfig._
    def migrate(scriptDir : String, dbConfig : DbConfig) : Try[Unit] = Try {
      val f = new Flyway
      f.setDataSource(createDataSource(dbConfig))
      f.setBaselineOnMigrate(true);
      f.setLocations(scriptDir)
      f.migrate()
    }
  }

  object DaoFactory {
    implicit def dbm2customerdao(dbm : DbManager) = {
      dbm.dbi.onDemand(classOf[CustomerDao])
    }

    implicit def dbm2certificatedao(dbm : DbManager) = {
      dbm.dbi.onDemand(classOf[CertificateDao])
    }

    implicit def dbm2testdao(dbm : DbManager) = {
      dbm.dbi.onDemand(classOf[TestDao])
    }
  }

  final case class DbConfig(user : String, password : String, jdbcUrl : String) {
    override def toString: String = s"user=$user, password=*******, jdbcUrl = $jdbcUrl"
  }

  object DbConfig {
    def createDataSource(dbConfig : DbConfig) : DataSource = {
      val ds = new HikariDataSource()
      ds.setDriverClassName("org.mariadb.jdbc.Driver")
      ds.setJdbcUrl(dbConfig.jdbcUrl)
      ds.setUsername(dbConfig.user)
      ds.setPassword(dbConfig.password)
      ds
    }
  }

}
