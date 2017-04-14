package com.toycert.db.model

import java.net.URI

import org.mindrot.jbcrypt.BCrypt

object Defs {
  final case class Customer(name : String, email : String, password : HashedPassword, id : Option[Int] = None) {
    override def toString = s"name=${name}, email=${email}"
  }

  final case class CertificateWithCutomerId(id : Int,
                                            commonName : String,
                                            privateKey : Array[Byte],
                                            certificateLink : URI,
                                            active : Boolean = true) {
    override def toString: String = s"CertificateWithCutomerId(${id},${commonName},${certificateLink},${active})"
  }


  trait HashedPassword {
    val value : String
  }

  object HashedPassword {
    def apply(raw : String) = new HashedPassword {
      override val value: String =  {
        BCrypt.hashpw(raw, BCrypt.gensalt())
      }
    }

    def alreadyHashed(hashedPassword : String) = {
      new HashedPassword {
        override val value: String = hashedPassword
      }
    }
  }

}
