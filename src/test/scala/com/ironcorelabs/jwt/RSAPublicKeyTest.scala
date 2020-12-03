package com.ironcorelabs.jwt

import scodec.bits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RSAPublicKeyTest extends AnyWordSpec with Matchers {
  "getPublicKey" should {
    "accept obviously invalid values" in {
      //This is just to reinforce the documentation information that obviously incorrect values will be left
      RSAPublicKey(hex"01", hex"") shouldBe 'left
    }
  }
}
