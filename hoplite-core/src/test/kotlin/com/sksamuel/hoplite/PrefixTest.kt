package com.sksamuel.hoplite

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

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

    test("handles defaults with prefixes against an empty config") {
      data class TestConfig(val a: String = "1")

      val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(PropertySource.map(emptyMap<String, String>()))
        .allowEmptyConfigFiles()
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("1")
    }

    test("strict mode should not report prefix key as unused") {
      data class TestConfig(val a: String, val b: Int)

      val config = ConfigLoaderBuilder.default()
        .strict()
        .addPropertySource(
          PropertySource.string(
            """
            foo.a = hello
            foo.b = 123
            """.trimIndent(), "props"
          )
        )
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "foo")

      config shouldBe TestConfig("hello", 123)
    }

    test("strict mode with nested prefix should not report prefix as unused") {
      data class TestConfig(val x: String)

      val config = ConfigLoaderBuilder.default()
        .strict()
        .addPropertySource(
          PropertySource.string(
            """
            database.primary.x = value
            """.trimIndent(), "props"
          )
        )
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "database.primary")

      config shouldBe TestConfig("value")
    }

    test("strict mode with prefix should still detect genuinely unused keys") {
      data class TestConfig(val a: String)

      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .strict()
          .addPropertySource(
            PropertySource.string(
              """
              foo.a = hello
              foo.b = unused_value
              """.trimIndent(), "props"
            )
          )
          .build()
          .loadConfigOrThrow<TestConfig>(prefix = "foo")
      }.message shouldContain "foo.b"
    }

    test("ConfigBinder with strict mode should not report prefix as unused") {
      data class DbConfig(val host: String, val port: Int)
      data class CacheConfig(val ttl: Int)

      val loader = ConfigLoaderBuilder.default()
        .strict()
        .addPropertySource(
          PropertySource.string(
            """
            db.host = localhost
            db.port = 5432
            cache.ttl = 60
            """.trimIndent(), "props"
          )
        )
        .build()

      val binder = loader.configBinder()
      binder.bindOrThrow<DbConfig>("db") shouldBe DbConfig("localhost", 5432)
      binder.bindOrThrow<CacheConfig>("cache") shouldBe CacheConfig(60)
    }

    test("strict mode with nonexistent prefix should not throw on prefix path") {
      data class TestConfig(val a: String = "default")

      val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(PropertySource.map(emptyMap<String, String>()))
        .allowEmptyConfigFiles()
        .build()
        .loadConfigOrThrow<TestConfig>(prefix = "nonexistent")

      config shouldBe TestConfig("default")
    }
  }
}
