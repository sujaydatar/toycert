package com.toycert.db.dao

import java.sql.ResultSet

import com.toycert.Utils.Exceptions.{EntityExistsException, EntityNotFoundException}
import com.toycert.Utils._
import com.toycert.db.DbUtils._
import com.toycert.db.DbUtils.DaoFactory._
import com.toycert.db.model.Defs._
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException
import org.skife.jdbi.v2.sqlobject.customizers.Mapper
import org.skife.jdbi.v2.sqlobject.{Bind, GetGeneratedKeys, SqlQuery, SqlUpdate}
import org.skife.jdbi.v2.tweak.ResultSetMapper
import com.toycert.db.model.Defs.HashedPassword._

import scala.util.Try

trait CustomerDao {
  @SqlUpdate("INSERT INTO customers (name, email, password) VALUES(:name, :email, :password)")
  @GetGeneratedKeys
  def createCustomerNonIdempotent(@Bind("name") name : String,
                               @Bind("email") email : String,
                               @Bind("password") password : String) : Int

  @SqlUpdate("DELETE FROM customers WHERE id=:id")
  def deleteCustomerById(@Bind("id") id : Int) : Unit

  @SqlQuery("SELECT id, name, email, password FROM customers WHERE id=:id")
  @Mapper(classOf[CustomerMapper])
  def getCustomerById(@Bind("id") id : Int) : Customer

  @SqlUpdate("DELETE FROM customers")
  def deleteAll() : Unit
}

object CustomerDao {
  def createCustomerNonIdempotent(customer : Customer)(implicit dbm : DbManager) : Try[Int] = {
    Try {
      dbm.createCustomerNonIdempotent(customer.name, customer.email, customer.password.value)
    }.leftMap(t => t match {
      case scve : UnableToExecuteStatementException =>
        new EntityExistsException(s"customer with email=${customer.email} already exists")
      case t => t
    })
  }

  def deleteCustomerById(id : Int)(implicit dbm : DbManager): Unit = {
    dbm.deleteCustomerById(id)
  }

  def getCustomerById(id : Int)(implicit dbm : DbManager) : Try[Customer] = Try {
    val result = dbm.getCustomerById(id)
    if(result == null)
        throw new EntityNotFoundException(s"customer with id=$id does not exist")
    result
  }

  def deleteAllCustomers()(implicit dbm : DbManager) : Unit = {
    dbm.deleteAll()
  }
}

class CustomerMapper extends ResultSetMapper[Customer] {
  override def map(index: Int, r: ResultSet, ctx: StatementContext): Customer = {
    val email = r.getString("email")
    val name = r.getString("name")
    val password = r.getString("password")
    val id = r.getInt("id")
    Customer(email = email, name = name, password = alreadyHashed(password), id = Some(id))
  }
}