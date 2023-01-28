package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BooleanArrayDecoderTest : StringSpec({

  "boolean array decoder should decode delimited strings" {
    data class Test(val a: BooleanArray)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_boolean_array.props")
    config.a shouldBe booleanArrayOf(true, false, true)
  }
})
