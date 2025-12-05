package com.sksamuel.hoplite

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

internal data class Github489Config(
  val list: List<Github489Variants>
)

 internal sealed class Github489Variants {
  data object VariantA : Github489Variants()
  data class VariantB(val value: Int) : Github489Variants()
}

@OptIn(ExperimentalHoplite::class)
class Github489 : BehaviorSpec({


  given("A config that uses sealed class variants") {
    val yaml = "/github489_default.yml"

    `when`("when deserializing the config with strict") {
      val config =
        ConfigLoaderBuilder.default()
          .strict()
          .withExplicitSealedTypes()
          .build()
          .loadConfigOrThrow<Github489Config>(yaml)

      then("it should not throw an unused config value error for discriminator (default discriminator)") {

        config shouldBe
          Github489Config(
            list = listOf(
              Github489Variants.VariantA,
              Github489Variants.VariantB(100)
            )
          )
      }
    }

  }


  given("A config that uses sealed class variants") {
    val yaml = "/github489_custom.yml"

    `when`("when deserializing the config with strict") {
      val config =
        ConfigLoaderBuilder.default()
          .strict()
          .withExplicitSealedTypes("desc")
          .build()
          .loadConfigOrThrow<Github489Config>(yaml)

      then("it should not throw an unused config value error for discriminator (custom 'desc')") {

        config shouldBe
          Github489Config(
            list = listOf(
              Github489Variants.VariantA,
              Github489Variants.VariantB(100)
            )
          )
      }
    }

  }
})
