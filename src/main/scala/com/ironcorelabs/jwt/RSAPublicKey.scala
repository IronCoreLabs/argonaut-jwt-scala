package com.ironcorelabs.jwt

import scodec.bits.ByteVector
import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec
import java.security.{PublicKey, KeyFactory}

object RSAPublicKey { //scalastyle:off 
  private val keyFactory = KeyFactory.getInstance("RSA")
  private def bytesToBigInt(bytes: ByteVector) = new BigInteger(1, bytes.toArray)
  /**
   * Convert a publicKey (usually referred to as n) and the exponent (e) to a public RSA key. Note that this conversion
   * will always succeed, but if you generate something that's invalid the JWT validation will fail.
   */
  def apply(publicKey: ByteVector, e: ByteVector): PublicKey = {
    keyFactory.generatePublic(new RSAPublicKeySpec(bytesToBigInt(publicKey), bytesToBigInt(e)))
  }
}
