package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigAliasNormalizationTest : FunSpec({
  data class AliasConfig(
    @ConfigAlias("fooBarAlias") val fooBar: String,
  )

  test("parsers should support @ConfigAlias with field normalization") {
    val config = ConfigLoader().loadConfigOrThrow<AliasConfig>("/alias_normalization.toml")
    config.fooBar shouldBe "Tom Preston-Werner"
  }
})
