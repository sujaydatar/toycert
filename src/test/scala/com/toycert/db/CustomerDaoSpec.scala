package com.toycert.db

import com.toycert.db.dao.CustomerDao._
import com.toycert.db.model.Defs._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CustomerDaoSpec extends DbSpec {
  "customer" should "be created" in {
    val list = Seq(
        Customer("John Doe", "johndoe@foo1.com", HashedPassword("foobar")),
        Customer("Jane Doe", "janedoe@boo1.com", HashedPassword("moobar")),
        Customer("Fred Doe", "freddoe@moo1.com", HashedPassword("fredbar"))
      )

    val customerIds = list.map(c => createCustomerNonIdempotent(c).get)

    val readCustomers = customerIds.map(id => getCustomerById(id).get)

    list.zip(readCustomers).foreach(tup => (tup._1.name, tup._1.email) should equal( (tup._2.name, tup._2.email)))
  }

  "creating customer with existing email" should  "fail" in {
    val c1 = Customer("John Doe", "johndoe@foo2.com", HashedPassword("foobar"))
    val c2 = Customer("John Doe J2", "johndoe@foo2.com", HashedPassword("foobar1"))

    assert(createCustomerNonIdempotent(c1).isSuccess)

    assert(createCustomerNonIdempotent(c1).isFailure)

    assert(createCustomerNonIdempotent(c2).isFailure)
  }

  "customers" should "be deleted" in {
    val list = Seq(
      Customer("John Doe", "johndoe@foo.com", HashedPassword("foobar")),
      Customer("Jane Doe", "janedoe@goo.com", HashedPassword("moobar")),
      Customer("Fred Doe", "freddoe@boo.com", HashedPassword("fredbar"))
    )

    val ids = list map(c => createCustomerNonIdempotent(c).get)

    ids.foreach(id => deleteCustomerById(id))

    ids.foreach(id => getCustomerById(id).isFailure should equal(true))
  }
}
