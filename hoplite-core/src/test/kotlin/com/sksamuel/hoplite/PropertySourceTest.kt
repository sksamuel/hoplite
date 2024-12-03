package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class PropertySourceTest : FunSpec() {
  init {

    test("reads config from string") {
      data class TestConfig(val a: String, val b: Int)

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          a = A value
          b = 42
          """.trimIndent(), "props"
          )
        )
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

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.stream(stream, "props"))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("A value", 42)
    }

    test("reads config from map") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      val arguments = mapOf(
        "a" to "A value",
        "b" to "42",
        "other" to listOf("Value1", "Value2")
      )

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.map(arguments))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
    }

    test("reads config from command line") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      val arguments = arrayOf(
        "--a=A value",
        "--b=42",
        "some other value",
        "--other=Value1",
        "--other=Value2"
      )

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.commandLine(arguments))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
    }

    test("reads from added source before default sources") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      withEnvironment(mapOf("b" to "91", "other" to "Random13")) {

        val arguments = arrayOf(
          "--a=A value",
          "--b=42",
          "some other value",
          "--other=Value1",
          "--other=Value2"
        )

        val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
          .addPropertySource(PropertySource.commandLine(arguments))
          .addDefaultPropertySources()
          .build()
          .loadConfigOrThrow<TestConfig>()

        config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
      }
    }

    test("reads from default source before specified") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      withEnvironment(mapOf("b" to "91", "other" to "Random13")) {
        val arguments = arrayOf(
          "--a=A value",
          "--b=42",
          "some other value",
          "--other=Value1",
          "--other=Value2"
        )

        val config = ConfigLoaderBuilder.default()
          .addDefaultPropertySources()
          .addPropertySource(PropertySource.commandLine(arguments))
          .build()
          .loadConfigOrThrow<TestConfig>()

        config shouldBe TestConfig("A value", 91, listOf("Random13"))
      }
    }
  }
}
