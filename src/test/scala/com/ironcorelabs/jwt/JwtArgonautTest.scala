package com.ironcorelabs.jwt

import org.scalatest.{WordSpec, TryValues, OptionValues, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtClaim}
import java.time.Instant
import argonaut._, Argonaut._

class JwtArgonautTest extends WordSpec with TryValues with OptionValues with Matchers {
  "JwtArgonautTest" should {
    val algo = JwtAlgorithm.HS256
    val key = "fookey"
    val time = 1488130700L
    "be able to encode " in {
      val inputJson = s"""{"expires":$time,"otherKey":true}"""
      val expectedEncoding = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHBpcmVzIjoxNDg4MTMwNzAwLCJvdGhlcktleSI6dHJ1ZX0.TKds8P-ITb1bAwt0j0f-UW20qvx7wypCStIerY0dPkI"
      val claimJson = Parse.parseOption(inputJson).value
      //Result manually computed on jwt.io
      JwtArgonaut.encode(claimJson, key, algo) shouldBe expectedEncoding
      JwtArgonaut.decodeJson(expectedEncoding, key, Seq(algo)).success.get.nospaces shouldBe inputJson
    }
  }
}
