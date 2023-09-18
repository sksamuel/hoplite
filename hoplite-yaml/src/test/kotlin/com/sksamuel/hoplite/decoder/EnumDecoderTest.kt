package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.fp.Validated
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class EnumDecoderTest : BehaviorSpec({

  data class Test(val a: Wine, val b: Wine)

  given("yml file with invalid enum name case") {
    val yaml = "/enums.yml"

    `when`("using case sensitive decoding") {
      val config = ConfigLoaderBuilder.default()
        .build()
        .loadConfig<Test>(yaml)

      then("should return an error") {
        config.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
          .error.shouldBeInstanceOf<ConfigFailure.DataClassFieldErrors>()
          .errors.list.shouldHaveSize(1)
          .first().shouldBeInstanceOf<ConfigFailure.ParamFailure>()
          .error.shouldBeInstanceOf<ConfigFailure.InvalidEnumConstant>()
      }
    }

    `when`("using case insensitive decoding") {
      val config = ConfigLoaderBuilder.default()
        .withResolveTypesCaseInsensitive()
        .build()
        .loadConfig<Test>(yaml)

      then("should load the config") {
        config.shouldBeInstanceOf<Validated.Valid<Test>>()
          .value.asClue {
            it.a shouldBe Wine.Malbec
            it.b shouldBe Wine.Merlot
          }
      }
    }
  }
})
