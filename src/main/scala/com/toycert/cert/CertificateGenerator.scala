package com.toycert.cert

import java.io.StringWriter
import java.math.BigInteger
import java.security._
import java.security.cert.X509Certificate
import java.util.Date

import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.{X500Name, X500NameBuilder}
import org.bouncycastle.cert.jcajce.{JcaX509CertificateConverter, JcaX509v3CertificateBuilder}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

object Provider {
  Security.addProvider(new BouncyCastleProvider());
  val name = BouncyCastleProvider.PROVIDER_NAME
}

object CertificateGenerator {
  final case class CertificateData(email : String,
                                   commonName : String,
                                   country : String,
                                   state : String,
                                   location : String,
                                   organization : String,
                                   validityStart : Date,
                                   validityEnd : Date)

  type CreatedCertData = (String,Array[Byte] )

  import KeyPairCreator._
  private val signatureAlgorithm = "SHA256WithRSAEncryption"

  def buildCertificate(data : CertificateData) : CreatedCertData = {
    val kp = createKeyPair("RSA")
    val cert = buildCertificate(data.toX500(), kp, new SecureRandom(), data.validityStart, data.validityEnd)
    (cert, kp.getPrivate.serialize())
  }

  private def buildCertificate(name : X500Name, keypair : KeyPair,
                               sr : SecureRandom,
                               before : Date,
                               after : Date) : String = {



    val builder = new JcaX509v3CertificateBuilder(
      name,
      sr.toSerial(),
      before,
      after,
      name,
      keypair.getPublic());

    val signer = keypair.getPrivate.toContentSigner()

    val certHolder = builder.build(signer);

    new JcaX509CertificateConverter().setProvider(Provider.name).getCertificate(certHolder).serialize()
  }

  private implicit class str2x500(req : CertificateData) {
    def toX500() : X500Name = {
      val builder = new X500NameBuilder(BCStyle.INSTANCE)
      builder.addRDN(BCStyle.CN, req.commonName)
      builder.addRDN(BCStyle.C, req.country)
      builder.addRDN(BCStyle.ST, req.state)
      builder.addRDN(BCStyle.O, req.organization)
      builder.addRDN(BCStyle.L, req.location)
      builder.build()
    }
  }

  private implicit class sr2serial(sr : SecureRandom) {
    def toSerial() : BigInteger  = new BigInteger(64, sr)
  }

  private implicit class privatekey2contentsigner(key : PrivateKey) {
    def toContentSigner() : ContentSigner = {
      new JcaContentSignerBuilder(signatureAlgorithm).build(key);
    }
  }

  private implicit class cert2str(cert : X509Certificate) {
    def serialize() : String = {
      val sw = new StringWriter();
      val pw = new JcaPEMWriter(sw)
      pw.writeObject(cert)
      pw.flush()
      pw.close()
      return sw.toString
    }
  }

  private implicit class key2str(key : PrivateKey) {
    def serialize() : Array[Byte] = {
      key.getEncoded
    }
  }
}

object KeyPairCreator {
  def createKeyPair(algorithm : String) = {
    val gen = KeyPairGenerator.getInstance(algorithm, Provider.name)
    gen.initialize(1024)
    gen.generateKeyPair()
  }
}

