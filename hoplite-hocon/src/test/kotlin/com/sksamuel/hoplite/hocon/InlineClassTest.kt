package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec

inline class Port(val value: Int)
inline class Port2(val value: Boolean)

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
    }.message shouldBe "Error loading config because:\n" +
      "\n" +
      "    - Could not instantiate 'com.sksamuel.hoplite.hocon.`InlineClassTest\$1\$2\$Config`' because:\n" +
      "    \n" +
      "        - 'port': Inline type kotlin.Boolean is incompatible with a Long value: 9200 (/valuetype.conf:2)"
  }
})
