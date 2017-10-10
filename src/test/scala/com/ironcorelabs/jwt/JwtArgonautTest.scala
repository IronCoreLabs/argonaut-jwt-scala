package com.ironcorelabs.jwt

import org.scalatest.{WordSpec, TryValues, OptionValues, Matchers}
import pdi.jwt.JwtAlgorithm
import argonaut._
import org.bouncycastle.jce.spec.ECPrivateKeySpec

class JwtArgonautTest extends WordSpec with TryValues with OptionValues with Matchers {
  "JwtArgonautTest" should {
    val time = 1488130700L
    "be able to encode " in {
      val algo = JwtAlgorithm.HS256
      val key = "fookey"
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val expectedEncoding = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHBpcmVzIjoxNDg4MTMwNzAwLCJvdGhlcktleSI6dHJ1ZX0.TKds8P-ITb1bAwt0j0f-UW20qvx7wypCStIerY0dPkI"
      val claimJson = Parse.parseOption(inputJson).value
      //Result manually computed on jwt.io
      JwtArgonaut.encode(claimJson, key, algo) shouldBe expectedEncoding
      JwtArgonaut.decodeJson(expectedEncoding, key, Seq(algo)).success.value.nospaces shouldBe inputJson
    }

    "be able to use a public/private key pair" in {
      val algo = JwtAlgorithm.ES256
      val (privateKeyBytes, (publicX, publicY)) = P256PublicKeyVerifierTest.generateKeyPairBytes
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec))
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val claimJson = Parse.parseOption(inputJson).value

      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val result = P256PublicKeyVerifier(publicX, publicY).flatMap(JwtArgonaut.decodeJson(encoded, _, Seq(algo))).success.value
      result.nospaces shouldBe inputJson
    }

    "be able to use a public/private key pair2" in {
      import scodec.bits._
      val algo = JwtAlgorithm.ES256
      val privateKeyBytes = hex"0x21b4e62c8614730ccfebd18110481177b11a1594d8e520af4aadb356283a6d76"
      val publicX = hex"0x008877bd4c40e96ace8681d29bb18375a61cdfbf21608880b8543bb215ce2f03db"
      val publicY = hex"0x00beb6b0fc6d95c76bd727026b20b38bca41f773e1805998beb331061e5ba78f12"
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec))
      val inputJson = s"""{"iat":1489418925,"oid":"cheating patriots","vid":1,"sub":"tom fing brady"}"""
      val claimJson = Parse.parseOption(inputJson).value

      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val result = P256PublicKeyVerifier(publicX, publicY).flatMap(JwtArgonaut.decodeJson(encoded, _, Seq(algo))).success.value
      result.nospaces shouldBe inputJson
    }

    "fail for invalid publicKey" in {
      val algo = JwtAlgorithm.ES256
      val (privateKeyBytes, (publicX, publicY)) = P256PublicKeyVerifierTest.generateKeyPairBytes
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec))
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val claimJson = Parse.parseOption(inputJson).value
      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val differentPublicKey = P256PublicKeyVerifierTest.generateKeyPair.getPublic
      val result = JwtArgonaut.decodeJson(encoded, differentPublicKey, Seq(algo)).failure.exception
      result shouldBe a[pdi.jwt.exceptions.JwtValidationException]
    }
  }
  def toBigInteger(bv: scodec.bits.ByteVector) = new java.math.BigInteger(bv.toArray)
}
