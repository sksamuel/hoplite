package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.net.InetAddress

class InetAddressDecoderTest : StringSpec({
  "InetAddress decoded from TOML" {
    data class Test(val inet: InetAddress)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_inet.toml")
    config shouldBe Test(InetAddress.getByName("10.0.0.2"))
  }
})
