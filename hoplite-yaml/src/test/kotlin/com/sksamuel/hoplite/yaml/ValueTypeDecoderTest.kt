package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValueTypeDecoderTest : StringSpec({
  "single value field as value type" {

    // host.value doesn't exist in the config, but because it is a value type, the value of "host"
    // will be used instead
    data class Host(val value: String)
    data class Port(val value: Int)
    data class Server(val host: Host, val port: Port)
    data class Config(val server: Server)

    val config = ConfigLoader().loadConfigOrThrow<Config>("/value_type.yml")
    config shouldBe Config(Server(Host("localhost"), Port(1234)))
  }
})
