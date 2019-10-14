package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.InetAddress

class InetAddressDecoderTest : StringSpec({
  "InetAddress decoded from yaml" {
    data class Test(val a: InetAddress)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_ipnet.yml")
    config shouldBe Test(InetAddress.getByName("10.0.0.2"))
  }
})
