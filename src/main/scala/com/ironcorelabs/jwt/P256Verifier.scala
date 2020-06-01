package com.ironcorelabs.jwt

import org.bouncycastle.asn1.x9.{ECNamedCurveTable, X9ECParameters}
import scodec.bits._
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.jce.spec.{ECParameterSpec, ECPublicKeySpec}
import java.math.BigInteger
import java.security.{KeyFactory, PublicKey}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scala.util.Try

case class PointNotOnCurve(x: ByteVector, y: ByteVector) extends Exception with scala.util.control.NoStackTrace
class PublicKeyVerifier(val curveParams: X9ECParameters) {
  final val Curve: ECCurve = curveParams.getCurve
  final val CurveSpec: ECParameterSpec =
    new ECParameterSpec(Curve, curveParams.getG, curveParams.getN, curveParams.getH)
  final val BouncyCastleProvider: BouncyCastleProvider = new BouncyCastleProvider()
  final val Factory: KeyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider)

  /**
   * Get the PublicKey for x and y. Will return an IllegalArgementException if the
   * createPoint fails (aka if the x and y are very wrong). Will return PointNotOnCurve if the
   * created point wasn't on the curve defined by curveParams.
   *
   * This method ensures that x and y are both interpreted as positive numbers.
   */
  def apply(x: ByteVector, y: ByteVector): Try[PublicKey] =
    Try {
      val maybeValidPoint = Curve.createPoint(new BigInteger(1, x.toArray), new BigInteger(1, y.toArray))
      if (!maybeValidPoint.isValid)
        throw new PointNotOnCurve(x, y)
      else
        Factory.generatePublic(new ECPublicKeySpec(maybeValidPoint, CurveSpec))
    }
}

//If this exception happens it means the BouncyCastle lib is either screwed up or the P-256 was removed.
object P256PublicKeyVerifier //scalastyle:off
    extends PublicKeyVerifier(
      Option(ECNamedCurveTable.getByName("P-256")).getOrElse(throw new Exception("P-256 was not defined."))
    )
