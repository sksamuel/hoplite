package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvironmentVariablesOverridePropertySourceTest : FunSpec({

  test("env with config.override should be applied") {
    withEnvironment("config.override.e", "wibble") {
      data class Config(val e: String)
      ConfigLoader().loadConfigOrThrow<Config>("/basic.props").e shouldBe "wibble"
    }
  }

  test("env without config.override should not be applied") {
    withEnvironment("config.verride.e", "wibble") {
      data class Config(val e: String)
      ConfigLoader().loadConfigOrThrow<Config>("/basic.props").e shouldBe "5.5"
    }
  }
})
