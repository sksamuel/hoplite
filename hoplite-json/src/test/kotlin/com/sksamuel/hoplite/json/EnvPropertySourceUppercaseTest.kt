package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceUppercaseTest : FunSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  test("loading from envs") {
    withEnvironment(mapOf("CREDS.USERNAME" to "a", "CREDS.PASSWORD" to "c", "SOME_CAMEL_SETTING" to "c")) {
      ConfigLoader()
        .withPropertySource(EnvironmentVariablesPropertySource(true, true))
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "c"), "c")
    }
  }

  test("loading from envs with underscore separator") {
    withEnvironment(mapOf("CREDS__USERNAME" to "a", "CREDS__PASSWORD" to "b", "SOME_CAMEL_SETTING" to "c")) {
      ConfigLoader()
        .withPropertySource(EnvironmentVariablesPropertySource(true, true))
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
    }
  }

  test("loading from envs with lowercase names") {
    withEnvironment(mapOf("creds__username" to "a", "creds__password" to "d", "someCamelSetting" to "e")) {
      ConfigLoader()
        .withPropertySource(EnvironmentVariablesPropertySource(true, true))
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "d"), "e")
    }
  }
})
