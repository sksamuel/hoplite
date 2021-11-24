package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@JvmInline
value class Port(val value: Int)

@JvmInline
value class Port2(val value: Boolean)

class InlineClassTest : FunSpec({

  test("decoding into an inline type") {
    data class Config(val port: Port, val host: String)
    ConfigLoader().loadConfigOrThrow<Config>("/valuetype.conf") shouldBe Config(Port(9200), "localhost")
  }

  test("error when an inline type is incompatible") {
    data class Config(val port: Port2, val host: String)
    shouldThrow<ConfigException> {
      val config = ConfigLoader().loadConfigOrThrow<Config>("/valuetype.conf")
      println(config)
    }.message shouldBe """Error loading config because:

    - Could not instantiate 'com.sksamuel.hoplite.hocon.`InlineClassTest${'$'}1${'$'}2${"\$Config"}`' because:

        - 'port': Inline type kotlin.Boolean is incompatible with a Long value: 9200 (/valuetype.conf:2)"""
  }

  test("decoding into a nullable inline type") {
    data class Config(val port: Port?, val host: String)
    ConfigLoader().loadConfigOrThrow<Config>("/valuetype.conf") shouldBe Config(Port(9200), "localhost")
  }
})
