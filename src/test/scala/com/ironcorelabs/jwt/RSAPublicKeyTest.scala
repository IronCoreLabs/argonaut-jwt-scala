package com.ironcorelabs.jwt

import org.scalatest.WordSpec
import scodec.bits._

class RSAPublicKeyTest extends WordSpec {
  "getPublicKey" should {
    "accept obviously invalid values" in {
      //This is just to reinforce the documentation information that obviously incorrect values will not throw
      RSAPublicKey(hex"00", hex"00")
    }
  }
}
