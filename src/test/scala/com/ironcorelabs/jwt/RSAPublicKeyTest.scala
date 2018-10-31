package com.ironcorelabs.jwt

import org.scalatest.{WordSpec, Matchers}
import scodec.bits._

class RSAPublicKeyTest extends WordSpec with Matchers {
  "getPublicKey" should {
    "accept obviously invalid values" in {
      //This is just to reinforce the documentation information that obviously incorrect values will be left
      RSAPublicKey(hex"aaaaa", hex"aaaaaaaaaaaaaaaaaaaaa") shouldBe ('left)
    }
  }
}
