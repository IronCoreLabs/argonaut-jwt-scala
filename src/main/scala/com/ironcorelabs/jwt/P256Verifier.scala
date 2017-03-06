package com.ironcorelabs.jwt

import org.bouncycastle.asn1.x9.{X9ECParameters, ECNamedCurveTable}
import scodec.bits._
import org.bouncycastle.math.ec.{ECCurve, ECPoint}
import org.bouncycastle.jce.spec.{ECPublicKeySpec, ECParameterSpec}
import java.math.BigInteger
import java.security.{PublicKey, KeyFactory, KeyPairGenerator, SecureRandom}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scala.util.Try

case class PointNotOnCurve(x: ByteVector, y: ByteVector) extends Exception with scala.util.control.NoStackTrace

class PublicKeyVerifier(val curveParams: X9ECParameters) {
  final val Curve = curveParams.getCurve
  final val CurveSpec = new ECParameterSpec(Curve, curveParams.getG, curveParams.getN, curveParams.getH)
  final val BouncyCastleProvider = new BouncyCastleProvider()
  final val Factory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider)
  final val ZeroByte = hex"00"

  /**
   * Get the PublicKey for x and y. Will return an IllegalArgementException if the
   * createPoint fails (aka if the x and y are very wrong). Will return PointNotOnCurve if the
   * created point wasn't on the curve defined by curveParams.
   *
   * This method ensures that x and y are both padded with zeros to avoid common issues with negative number byte arrays.
   * These will be thrown away by BigInteger if they're not needed.
   */
  def apply(x: ByteVector, y: ByteVector): Try[PublicKey] = Try {
    val maybeValidPoint = Curve.createPoint(new BigInteger((ZeroByte ++ x).toArray), new BigInteger((ZeroByte ++ y).toArray))
    if (!maybeValidPoint.isValid) {
      throw new PointNotOnCurve(x, y)
    } else {
      Factory.generatePublic(new ECPublicKeySpec(maybeValidPoint, CurveSpec))
    }
  }
}

//If this exception happens it means the BouncyCastle lib is either screwed up or the P-256 was removed.
object P256PublicKeyVerifier
  extends PublicKeyVerifier(Option(ECNamedCurveTable.getByName("P-256")).getOrElse(throw new Exception("P-256 was not defined.")))
