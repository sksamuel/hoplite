package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceUppercaseTest : DescribeSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  describe("loading from envs") {
    it("with default options") {
      withEnvironment(mapOf("CREDS.USERNAME" to "a", "CREDS.PASSWORD" to "c", "SOME_CAMEL_SETTING" to "c")) {
        ConfigLoader()
          .withPropertySource(EnvironmentVariablesPropertySource(true, true))
          .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "c"), "c")
      }
    }
    it("with underscore separator") {
      withEnvironment(mapOf("CREDS__USERNAME" to "a", "CREDS__PASSWORD" to "b", "SOME_CAMEL_SETTING" to "c")) {
        ConfigLoader()
          .withPropertySource(EnvironmentVariablesPropertySource(true, true))
          .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
      }
    }
    it("with lowercase names") {
      withEnvironment(mapOf("creds__username" to "a", "creds__password" to "d", "someCamelSetting" to "e")) {
        ConfigLoader()
          .withPropertySource(EnvironmentVariablesPropertySource(true, true))
          .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "d"), "e")
      }
    }
  }
})
