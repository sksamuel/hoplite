package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceUnderscoreAsSeparatorTest : FunSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  test("loading from envs") {
    withEnvironment(mapOf("creds_username" to "a", "creds_password" to "b", "somecamelsetting" to "c")) {
      ConfigLoader()
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
    }
  }
})
