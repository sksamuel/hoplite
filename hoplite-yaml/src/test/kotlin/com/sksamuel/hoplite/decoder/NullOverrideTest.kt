package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NullOverrideTest : FunSpec() {
  init {
    test("ability to set value to null explicitly #377") {
      ConfigLoaderBuilder.default()
        .addPropertySource(YamlPropertySource("username: null"))
        .addPropertySource(YamlPropertySource("username: something"))
        .allowNullOverride()
        .build()
        .loadConfig<Config>() shouldBe Config(null).valid()
    }
  }
}

data class Config(val username: String?)
