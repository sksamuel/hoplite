package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class PrefixTest : FunSpec() {
  init {

    test("reads config from string at a given prefix") {
      data class TestConfig(val a: String, val b: Int)

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          foo.a = A value
          foo.b = 42
          bar.a = A value bar
          bar.b = 45
          """.trimIndent(), "props"
          )
        )
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("A value", 42)
    }

    test("reads config from input stream at a given prefix") {
      data class TestConfig(val a: String, val b: Int)

      val stream = """
          foo.a = A value
          foo.b = 42
          bar.a = A value bar
          bar.b = 45
          """.trimIndent().byteInputStream(Charsets.UTF_8)

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.stream(stream, "props"))
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("A value", 42)
    }

    test("reads config from map at a given prefix") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      val arguments = mapOf(
        "foo.a" to "A value",
        "foo.b" to "42",
        "bar.a" to "A value bar",
        "bar.b" to "45",
        "foo.other" to listOf("Value1", "Value2"),
        "bar.other" to listOf("Value1bar", "Value2bar")
      )

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.map(arguments))
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
    }

    test("reads config from command line at a given prefix") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      val arguments = arrayOf(
        "--foo.a=A value",
        "--foo.b=42",
        "--bar.a=A value bar",
        "--bar.b=45",
        "some other value",
        "--foo.other=Value1",
        "--foo.other=Value2",
        "--bar.other=Value1bar",
        "--bar.other=Value2bar",
        "--other=Value1o",
        "--other=Value2o"
      )

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.commandLine(arguments))
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
    }

    test("reads from added source before default sources at a given prefix") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      withEnvironment(mapOf("foo.b" to "91", "foo.other" to "Random13")) {

        val arguments = arrayOf(
          "--foo.a=A value",
          "--foo.b=42",
          "--bar.a=A value bar",
          "--bar.b=45",
          "some other value",
          "--foo.other=Value1",
          "--foo.other=Value2",
          "--bar.other=Value1bar",
          "--bar.other=Value2bar",
          "--other=Value1o",
          "--other=Value2o"
        )

        val config = ConfigLoaderBuilder.default()
          .addPropertySource(PropertySource.commandLine(arguments))
          .addDefaultPropertySources()
          .build()
          .loadConfigOrThrow<TestConfig>(prefix = "foo")

        config shouldBe TestConfig("A value", 42, listOf("Value1", "Value2"))
      }
    }

    test("reads from default source before specified at a given prefix") {
      data class TestConfig(val a: String, val b: Int, val other: List<String>)

      withEnvironment(mapOf("FOO_B" to "91", "FOO_OTHER" to "Random13")) {
        val arguments = arrayOf(
          "--foo.a=A value",
          "--foo.b=42",
          "--bar.a=A value bar",
          "--bar.b=45",
          "some other value",
          "--foo.other=Value1",
          "--foo.other=Value2",
          "--bar.other=Value1bar",
          "--bar.other=Value2bar",
          "--other=Value1o",
          "--other=Value2o"
        )

        val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
          .addDefaultPropertySources()
          .addPropertySource(PropertySource.commandLine(arguments))
          .build()
          .loadConfigOrThrow<TestConfig>(prefix = "foo")

        config shouldBe TestConfig("A value", 91, listOf("Random13"))
      }
    }
  }
}
