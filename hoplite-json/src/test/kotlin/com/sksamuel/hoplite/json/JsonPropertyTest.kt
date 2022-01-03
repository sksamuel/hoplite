package com.sksamuel.hoplite.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withSystemProperties
import io.kotest.matchers.shouldBe

class JsonPropertyTest : FunSpec({

  data class Config(@JsonProperty("foo") val a: String, val c: Long)

  test("json property on config fields") {
    withSystemProperties(mapOf("config.override.foo" to "x", "config.override.c" to "123")) {
      val config = ConfigLoader.Builder()
        .addParameterMapper(JsonPropertyParamMapper)
        .build()
        .loadConfigOrThrow<Config>()

      config shouldBe Config("x", 123L)
    }
  }

})
