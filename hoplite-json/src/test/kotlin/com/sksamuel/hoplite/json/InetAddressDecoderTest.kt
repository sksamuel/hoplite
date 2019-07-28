package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.InetAddress

class InetAddressDecoderTest : StringSpec({
  "InetAddress decoded from json" {
    data class Test(val a: InetAddress)
    ConfigLoader().loadConfig<Test>("/test_ipnet.json").shouldBeValid {
      it.a shouldBe Test(InetAddress.getByName("10.0.0.2"))
    }
  }
})
