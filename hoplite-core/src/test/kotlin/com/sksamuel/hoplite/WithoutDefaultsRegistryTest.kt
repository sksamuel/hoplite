package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf

class WithoutDefaultsRegistryTest : FunSpec() {
  init {
    test("default registry throws no error") {
      val loader = ConfigLoader {
        addMapSource(mapOf("custom_value" to "\${PATH}"))
      }
      val e = loader.loadConfig<Config>()
      e as Validated.Valid<Config>
      e.value.customValue shouldNotBe "\${path}"
    }

    test("empty sources registry throws error") {
      val loader = ConfigLoader {
        withDefaultSources(false)
        addMapSource(mapOf("custom_value" to "\${PATH}"))
      }
      val e = loader.loadConfig<Config>()
      e as Validated.Invalid<ConfigFailure>
      e.error shouldBe instanceOf(ConfigFailure.DataClassFieldErrors::class)
    }

    test("empty param mappers registry throws error") {
      val loader = ConfigLoader {
        withDefaultParamMappers(false)
        addMapSource(mapOf("custom_value" to "\${PATH}"))
      }

      val e = loader.loadConfig<Config>()
      e as Validated.Invalid<ConfigFailure>
      e.error shouldBe instanceOf(ConfigFailure.DataClassFieldErrors::class)
    }

    test("empty preprocessors registry throws error") {
      val loader = ConfigLoader {
        withDefaultPreprocessors(false)
        addMapSource(mapOf("custom_value" to "\${PATH}"))
      }
      val e = loader.loadConfig<Config>()
      e as Validated.Valid<Config>
      e.value.customValue shouldBe "\${PATH}"
    }

  }

  data class Config(val PATH: String, val customValue: String)
}
