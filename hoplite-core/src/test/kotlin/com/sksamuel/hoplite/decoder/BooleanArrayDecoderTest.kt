package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BooleanArrayDecoderTest : StringSpec({

  "boolean array decoder should decode delimited strings" {
    data class Test(val a: BooleanArray)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_boolean_array.props")
    config.a shouldBe booleanArrayOf(true, false, true)
  }

  "boolean array decoder should report invalid input as a config failure rather than throwing" {
    data class Test(val a: BooleanArray)

    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Test>("/test_boolean_array_invalid.props")
    }
  }
})
