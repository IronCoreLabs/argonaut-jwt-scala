package com.ironcorelabs.jwt

import org.scalatest.{Matchers, TryValues, WordSpec}
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import scodec.bits._

class P256PublicKeyVerifierTest extends WordSpec with TryValues with Matchers {
  "P256PublicKeyVerifier" should {
    "successfully convert valid public key with a 30 byte x" in {
      val (x, y) = hex"6c8bdf3cbbab30bff5467539ac462df79583f17aa828983f45c3d04c55ab" -> hex"00b213df136f2c7a987580e1bd0251b682cb13572488d27d4c9a148d9e1e7f0e6d"
      val publicKey = P256PublicKeyVerifier(x, y).success.value
      publicKey shouldBe a[BCECPublicKey]
      val qPoint = publicKey.asInstanceOf[BCECPublicKey].getQ
      val xResult = ByteVector.view(qPoint.getAffineXCoord().toBigInteger().toByteArray)
      val yResult = ByteVector.view(qPoint.getAffineYCoord().toBigInteger().toByteArray)
      xResult shouldBe x
      yResult shouldBe y
    }

    "fail for negative number which isn't on the curve" in {
      //8b causes it to fail because it doesn't have a leading 0 bit so it's negative
      val (x, y) = hex"8b" -> hex"12"
      val error = P256PublicKeyVerifier(x, y).failure.exception
      //Note that because the verifier pads the 0s onto the front it doesn't fail because of the exception thrown in Nat256 (in bouncy castle)
      error shouldBe an[PointNotOnCurve]
    }

    "fail for point not on curve" in {
      //Changed the first char of x to 5 from a 6
      val (x, y) = hex"5c8bdf3cbbab30bff5467539ac462df79583f17aa828983f45c3d04c55ab" -> hex"00b213df136f2c7a987580e1bd0251b682cb13572488d27d4c9a148d9e1e7f0e6d"
      val error = P256PublicKeyVerifier(x, y).failure.exception
      error shouldBe an[PointNotOnCurve]
      val PointNotOnCurve(errorX, errorY) = error.asInstanceOf[PointNotOnCurve]
      errorX shouldBe x
      errorY shouldBe y
    }

    "fail for point which is too big" in {
      //Changed the first char of x to 5 from a 6
      val (x, y) = ByteVector.concat(List.fill(33)(hex"25")) -> hex"00"
      val error = P256PublicKeyVerifier(x, y).failure.exception
      error shouldBe an[IllegalArgumentException] //Key was too large, see Nat256.fromBigInteger
    }
  }
}

object P256PublicKeyVerifierTest {
  import java.security.{KeyPairGenerator, SecureRandom}
  import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
  //SIDE EFFECT HO!
  val g = KeyPairGenerator.getInstance("ECDSA", P256PublicKeyVerifier.BouncyCastleProvider)
  g.initialize(P256PublicKeyVerifier.CurveSpec, new SecureRandom())

  def generateKeyPairBytes = {
    val keyPair = g.generateKeyPair
    val privateKey = keyPair.getPrivate.asInstanceOf[BCECPrivateKey]
    val publicKey = keyPair.getPublic.asInstanceOf[BCECPublicKey]
    val publicKeyPoint = publicKey.getQ
    val publicXBytes = ByteVector.view(publicKeyPoint.getAffineXCoord.toBigInteger.toByteArray)
    val publicYBytes = ByteVector.view(publicKeyPoint.getAffineYCoord.toBigInteger.toByteArray)
    (ByteVector.view(privateKey.getS.toByteArray), (publicXBytes, publicYBytes))
  }

  def generateKeyPair = g.generateKeyPair
}
