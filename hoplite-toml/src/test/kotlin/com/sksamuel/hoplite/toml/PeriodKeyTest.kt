package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PeriodKeyTest : FunSpec() {
  init {
    test("period in toml key should parseable") {
      val contents = """
value = 10
[values]
"cleanup.policy" = "compact"
"delete.retention.ms" = "604800000
""""
      ConfigLoader.builder()
        .addSource(TomlPropertySource(contents))
        .build()
        .loadConfigOrThrow<Configuration>()
        .values shouldBe mapOf("cleanup.policy" to "compact", "delete.retention.ms" to "604800000")
    }
  }
}

data class Configuration(
  val value: Int = 5,
  val values: Map<String, String> = mapOf(),
)
