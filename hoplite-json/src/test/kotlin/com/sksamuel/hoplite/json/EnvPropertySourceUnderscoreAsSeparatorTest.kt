package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addEnvironmentSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceUnderscoreAsSeparatorTest : FunSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  test("loading from envs") {
    withEnvironment(mapOf("creds__username" to "a", "creds__password" to "b", "some_camel_setting" to "c")) {
      ConfigLoader
        .builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
    }
  }
})
