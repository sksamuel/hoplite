package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PropertySourceTest : FunSpec() {
  init {

    test("reads config from string") {
      data class TestConfig(val a: String, val b: Int)
      val config = ConfigLoader.Builder()
        .addPropertySource(PropertySource.string("""
          a = A value
          b = 42
          """.trimIndent(), "props"))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("A value", 42)
    }

    test("reads config from input stream") {
      data class TestConfig(val a: String, val b: Int)

      val stream = """
          a = A value
          b = 42
          """.trimIndent().byteInputStream(Charsets.UTF_8)

      val config = ConfigLoader.Builder()
        .addPropertySource(PropertySource.stream(stream, "props"))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("A value", 42)
    }

  }
}
