package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigEnumDefault
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.fp.Validated
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@ConfigEnumDefault("Unknown")
enum class Color { Red, Blue, Green, Unknown }

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

  given("an enum annotated with @ConfigEnumDefault") {
    data class Branding(val bgColor: Color)

    `when`("the yaml has an unknown enum value") {
      val config = ConfigLoaderBuilder.default()
        .build()
        .loadConfig<Branding>("/enum_default_invalid.yml")

      then("it falls back to the configured default") {
        config.shouldBeInstanceOf<Validated.Valid<Branding>>()
          .value.bgColor shouldBe Color.Unknown
      }
    }

    `when`("the yaml has a known enum value") {
      val config = ConfigLoaderBuilder.default()
        .build()
        .loadConfig<Branding>("/enum_default_valid.yml")

      then("it decodes normally") {
        config.shouldBeInstanceOf<Validated.Valid<Branding>>()
          .value.bgColor shouldBe Color.Red
      }
    }
  }
})
