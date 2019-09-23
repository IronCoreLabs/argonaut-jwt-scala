package com.ironcorelabs.jwt

import org.scalatest.{Matchers, OptionValues, TryValues, WordSpec}
import pdi.jwt.JwtAlgorithm
import argonaut._
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import scodec.bits.{Bases, ByteVector}

class JwtArgonautTest extends WordSpec with TryValues with OptionValues with Matchers {
  "JwtArgonautTest" should {
    val time = 1488130700L
    "be able to encode " in {
      val algo = JwtAlgorithm.HS256
      val key = "fookey"
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val expectedEncoding =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHBpcmVzIjoxNDg4MTMwNzAwLCJvdGhlcktleSI6dHJ1ZX0.TKds8P-ITb1bAwt0j0f-UW20qvx7wypCStIerY0dPkI"
      val claimJson = Parse.parseOption(inputJson).value
      //Result manually computed on jwt.io
      JwtArgonaut.encode(claimJson, key, algo) shouldBe expectedEncoding
      JwtArgonaut.decodeJson(expectedEncoding, key, Seq(algo)).success.value.nospaces shouldBe inputJson
    }

    "be able to use a public/private key pair" in {
      val algo = JwtAlgorithm.ES256
      val (privateKeyBytes, (publicX, publicY)) = P256PublicKeyVerifierTest.generateKeyPairBytes
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(
        new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec)
      )
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val claimJson = Parse.parseOption(inputJson).value

      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val result =
        P256PublicKeyVerifier(publicX, publicY).flatMap(JwtArgonaut.decodeJson(encoded, _, Seq(algo))).success.value
      result.nospaces shouldBe inputJson
    }

    "be able to use a public/private key pair2" in {
      import scodec.bits._
      val algo = JwtAlgorithm.ES256
      val privateKeyBytes = hex"0x21b4e62c8614730ccfebd18110481177b11a1594d8e520af4aadb356283a6d76"
      val publicX = hex"0x008877bd4c40e96ace8681d29bb18375a61cdfbf21608880b8543bb215ce2f03db"
      val publicY = hex"0x00beb6b0fc6d95c76bd727026b20b38bca41f773e1805998beb331061e5ba78f12"
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(
        new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec)
      )
      val inputJson = s"""{"iat":1489418925,"oid":"cheating patriots","vid":1,"sub":"tom fing brady"}"""
      val claimJson = Parse.parseOption(inputJson).value

      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val result =
        P256PublicKeyVerifier(publicX, publicY).flatMap(JwtArgonaut.decodeJson(encoded, _, Seq(algo))).success.value
      result.nospaces shouldBe inputJson
    }

    "fail for invalid publicKey" in {
      val algo = JwtAlgorithm.ES256
      val (privateKeyBytes, (publicX, publicY)) = P256PublicKeyVerifierTest.generateKeyPairBytes
      val privateKey = P256PublicKeyVerifier.Factory.generatePrivate(
        new ECPrivateKeySpec(toBigInteger(privateKeyBytes), P256PublicKeyVerifier.CurveSpec)
      )
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val claimJson = Parse.parseOption(inputJson).value
      val encoded = JwtArgonaut.encode(claimJson, privateKey, algo)
      val differentPublicKey = P256PublicKeyVerifierTest.generateKeyPair.getPublic
      val result = JwtArgonaut.decodeJson(encoded, differentPublicKey, Seq(algo)).failure.exception
      result shouldBe a[pdi.jwt.exceptions.JwtValidationException]
    }

    "verify signature and detect expired jwt on RS256 value using RSAPublicKey" in {
      val algo = JwtAlgorithm.RS256
      val jwt =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlEwWXhNekUwTlVJeE9UVTNRakZFTlRZM01rVkNRakE0UkVNMk1UTkZOVGRETVRBNE9EQTVNUSJ9.eyJodHRwOi8vaXJvbmNvcmUva2lkIjozNSwiaHR0cDovL2lyb25jb3JlL3NpZCI6MjIsImh0dHA6Ly9pcm9uY29yZS91aWQiOiJlcm5pZS50dXJuZXJAaXJvbmNvcmVsYWJzLmNvbSIsImlzcyI6Imh0dHBzOi8vaXJvbmNvcmVsYWJzLmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDExMjYyNDQyMzY2MzI1NDQ1Mjg5NCIsImF1ZCI6ImhHRUx4dUJLRDY0bHRTNFZOYUl5Mm16Vnd0cWdKYTVmIiwiaWF0IjoxNTQwNDg3MDg0LCJleHAiOjE1NDA1MjMwODQsImF0X2hhc2giOiJPcE53aXh6NU5fMnBxNzNWbDQxbTJRIiwibm9uY2UiOiI4VFJRckdWWnZyLWdBVHJKdWdWYUw1bzVKaE1lVTJZLSJ9.h5wKJjGFhuo6VmXoDPs-9yoavo3z_lWLkyibXKPr0DsJS21F6q-KDHJC9NK7P6Fh4EP0D-BJUn4I7YIZlKwv1kYS9rHK_sWVUlGBOEXzPDFPgaSyNCRBPcgJPwoAe179YNntM9CFinmswLoFBRe8tan0w42MiT9Yt5HQ45MP_2roE6qYUuJLM92Vs9Z4c2A0zCnDtfO0m2duaV_LZWL4SNKMyUsz3TA_OCVGOP1g0HoUqSluBWGcPYHeUC1F8OWPYunWeB0q55zalWoJ8U-HHeX9gUupFiUBzAycXkU5tGDoOvjsFgVhWGHRxvrRZBJDA7LVG2nnzCPfM0V3hj0QZA"
      val publicKeyBytes = ByteVector
        .fromBase64(
          "sAFHaLpCNp1ZnB_lqCP7aVSLleJbyFcCGj8rv5EQi8JjvqXs78hMwbaKRHB-09hxMhCzBhn7M2rfLynP2xOassP3RS9B0HlA4rf_XvvUY_aJHrJkRKe7GNfGzHW5KBqYXysle69LpXjYXcvQt7nqRyoZMpgyum1yNFwp4iunSekjAXfC_Z7yBgAQjCWLJ_c7WZvDLdHDw7hmihXGVIej6G7PMjmTs9d8T_1FzFYJwdmofsNHHXh8gNfNtBFfBcXeKjYyqwHmR1UHRL_eSlhbq7Rl4GoCfx3386yeFoBJ-ER3ljWx7QyjEeGkLOq3oNvh7-WbVqAm38aZ2LhBklzS8Q",
          Bases.Alphabets.Base64Url
        )
        .value
      val e = ByteVector.fromBase64("AQAB").value
      val publicKey = RSAPublicKey(publicKeyBytes, e).right.get
      JwtArgonaut
        .decode(jwt, publicKey, List(algo))
        .toEither
        .left
        .get shouldBe a[pdi.jwt.exceptions.JwtExpirationException]
    }
  }
  def toBigInteger(bv: scodec.bits.ByteVector) = new java.math.BigInteger(bv.toArray)
}
