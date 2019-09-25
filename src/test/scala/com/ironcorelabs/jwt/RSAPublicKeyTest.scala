package com.ironcorelabs.jwt

import org.scalatest.{Matchers, WordSpec}
import scodec.bits._

class RSAPublicKeyTest extends WordSpec with Matchers {
  "getPublicKey" should {
    "accept obviously invalid values" in {
      //This is just to reinforce the documentation information that obviously incorrect values will be left
      RSAPublicKey(hex"01", hex"") shouldBe ('left)
    }
  }
}
