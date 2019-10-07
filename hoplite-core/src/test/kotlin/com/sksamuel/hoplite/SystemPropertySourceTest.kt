package com.sksamuel.hoplite

import io.kotlintest.extensions.system.withSystemProperties
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class SystemPropertySourceTest : FunSpec() {

  data class TestConfig(val foo: String, val woo: String)

  init {
    test("loading from sys props") {
      withSystemProperties(mapOf("foo" to "a", "woo" to "b")) {
        ConfigLoader().loadConfigOrThrow<TestConfig>() shouldBe TestConfig("", "")
      }
    }
  }
}
