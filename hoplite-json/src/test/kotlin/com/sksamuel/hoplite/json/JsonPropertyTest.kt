package com.sksamuel.hoplite.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.extensions.system.withSystemProperties
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.FunSpec

class JsonPropertyTest : FunSpec({

  data class Config(@JsonProperty("foo") val a: String, val c: Long)

  test("json property on config fields") {
    withSystemProperties(mapOf("config.override.foo" to "x", "config.override.c" to "123")) {
      ConfigLoader().withParameterMapper(JsonPropertyParamMapper).loadConfigOrThrow<Config>() shouldBe Config("x", 123L)
    }
  }

})
