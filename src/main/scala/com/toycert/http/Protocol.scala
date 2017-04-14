package com.toycert.http

object Protocol {
  trait ServiceResponse

  final case class CertificateStateChangedEvent(event : String, id : Int)

  final case class ChangeCertificateStateRequest(active : Boolean, callbackUrl : String)

  final case class CreateCertificateRequest(commonName : String,
                                            organization : String,
                                            country : String,
                                            state : String,
                                            location : String)


  final case class CreateCertificateResponse(id : Int)

  final case class CreateCustomerRequest(name : String, email : String, password : String)

  final case class CreateCustomerResponse(id : Int) extends ServiceResponse

  final case class ErrorResponse(errorMessage : String) extends ServiceResponse

  final case class GetCertByStateResponse(certificates : List[String])

  final case class LightCertificate(commonName : String)

  final case class LightCustomer(id : Int, name : String, email : String)
}
