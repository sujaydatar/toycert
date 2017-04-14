package com.toycert.db.dao

import com.toycert.db.DbUtils.DbManager
import org.skife.jdbi.v2.sqlobject.SqlQuery

trait TestDao {
  @SqlQuery("SELECT 1")
  def test() : Int
}

object TestDao {
  def test()(implicit dbm : DbManager) : Int = {
    val dao = dbm.dbi.onDemand(classOf[TestDao])
    dao.test()
  }
}
