package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addIdiomaticEnvironmentSource
import com.sksamuel.hoplite.transformer.PathNormalizer
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceSingleUnderscoreAsSeparatorTest : FunSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  test("loading from envs") {
    withEnvironment(mapOf("creds_username" to "a", "creds_password" to "b", "someCamelSetting" to "c")) {
      ConfigLoader
        .builder()
        .addIdiomaticEnvironmentSource()
        .build()
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
    }
  }

  test("loading from envs with a path normalizer") {
    withEnvironment(mapOf("CREDS_USERNAME" to "a", "CREDS_PASSWORD" to "b", "SOMECAMELSETTING" to "c")) {
      ConfigLoader
        .builder()
        .addNodeTransformer(PathNormalizer)
        .addIdiomaticEnvironmentSource()
        .build()
        .loadConfigOrThrow<Config>() shouldBe Config(Creds("a", "b"), "c")
    }
  }
})
