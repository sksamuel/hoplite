package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import io.kotest.matchers.types.shouldBeInstanceOf

class WithoutDefaultsRegistryTest : FunSpec() {
  init {
    test("default registry throws no error") {
      val loader = ConfigLoaderBuilder
        .defaultWithoutPropertySources()
        .addMapSource(mapOf("custom_value" to "\${PATH}", "PATH" to "\${PATH}"))
        .build()

      val e = loader.loadConfig<Config>()
      e.shouldBeInstanceOf<Validated.Valid<Config>>()
      e.value.customValue shouldNotBe "\${path}"
    }

    test("empty sources registry throws error") {
      val loader = ConfigLoaderBuilder
        .defaultWithoutPropertySources()
        .addMapSource(mapOf("custom_value" to "\${PATH}"))
        .build()

      val e = loader.loadConfig<Config>()
      e as Validated.Invalid<ConfigFailure>
      e.error shouldBe instanceOf(ConfigFailure.DataClassFieldErrors::class)
    }

    test("empty param mappers registry throws error") {
      val loader = ConfigLoaderBuilder
        .defaultWithoutPropertySources()
        .addMapSource(mapOf("custom_value" to "\${PATH}"))
        .build()

      val e = loader.loadConfig<Config>()
      e as Validated.Invalid<ConfigFailure>
      e.error shouldBe instanceOf(ConfigFailure.DataClassFieldErrors::class)
    }

//    test("empty preprocessors registry throws error") {
//      val loader = ConfigLoaderBuilder.empty {
//        addMapSource(mapOf("custom_value" to "\${PATH}", "PATH" to "\${PATH}"))
//      }.build()
//      val e = loader.loadConfigOrThrow<Config>()
//      e.customValue shouldBe "\${PATH}"
//    }

  }
   // if your env vars is not "PATH" and is "Path" auto inject doesn't work
  data class Config(val PATH: String, val customValue: String)
}
