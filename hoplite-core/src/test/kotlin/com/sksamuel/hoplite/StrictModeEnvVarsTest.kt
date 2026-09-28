package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class StrictModeEnvVarsTest : FunSpec({

  data class TestConfig(val name: String)

  // gh-505: process-wide environment variables (HOME, USER, TMPDIR, ...) should never be
  // reported as unused in strict mode — strict is meant to catch stale values in user-provided
  // config, not noise from the OS.
  test("strict mode should ignore unused environment variables") {
    val config = ConfigLoaderBuilder.default()
      .addMapSource(mapOf("name" to "test"))
      .strict()
      .build()
      .loadConfigOrThrow<TestConfig>()

    config.name shouldBe "test"
  }

  test("strict mode should ignore env vars supplied to a custom EnvironmentVariablesPropertySource") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource(
        environmentVariableMap = { mapOf("name" to "test", "OTHER" to "noise") },
      ))
      .strict()
      .build()
      .loadConfigOrThrow<TestConfig>()

    config.name shouldBe "test"
  }

  test("strict mode should ignore unused JVM system properties") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(SystemPropertiesPropertySource {
        mapOf("config.override.unused" to "noise")
      })
      .addMapSource(mapOf("name" to "test"))
      .strict()
      .build()
      .loadConfigOrThrow<TestConfig>()

    config.name shouldBe "test"
  }

  test("strict mode still flags unused values from user-provided map sources") {
    val ex = shouldThrow<ConfigException> {
      ConfigLoaderBuilder.default()
        .addMapSource(mapOf("name" to "test", "stale" to "value"))
        .strict()
        .build()
        .loadConfigOrThrow<TestConfig>()
    }
    ex.message
      .shouldContain("Config value 'stale'")
      .shouldNotContain("Config value 'HOME'")
      .shouldNotContain("Config value 'USER'")
  }
})
