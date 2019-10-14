package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.decoder.ValueType
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec

class ValueClassTest : FunSpec({

  test("decoding into a value type") {
    data class Port(val value: Int) : ValueType
    data class Config(val port: Port, val host: String)
    ConfigLoader().loadConfigOrThrow<Config>("/valuetype.conf") shouldBe Config(Port(9200), "localhost")
  }

  test("error when a value type is incompatible") {
    data class Port(val value: Boolean) : ValueType
    data class Config(val port: Port, val host: String)
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Config>("/valuetype.conf")
    }.message shouldBe "Error loading config because:\n" +
      "\n" +
      "    - Could not instantiate 'com.sksamuel.hoplite.hocon.`ValueClassTest\$1\$2\$Config`' because:\n" +
      "    \n" +
      "        - 'port': Value type kotlin.Boolean is incompatible with a Long value: 9200 (/valuetype.conf:2)"
  }
})
