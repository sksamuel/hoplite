package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapTest : FunSpec() {
  init {
    test("allow maps to be decoded from lists of two values of key/value pairs") {
      data class Test(val a: Map<String, String>)

      val config = ConfigLoaderBuilder.default()
        .addPropertySource(JsonPropertySource(
          """ {
            "a": [
              {
                "key": "arnold",
                "value": "rimmer"
              },
              {
                "key": "mr",
                "value": "flibble"
              }
            ]
          } """
        ))
        .build()
        .loadConfigOrThrow<Test>()
      config shouldBe Test(mapOf("arnold" to "rimmer", "mr" to "flibble"))
    }
  }
}
