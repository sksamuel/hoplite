package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PairDecoderTest : StringSpec({
  "pair decoded from toml" {
    data class Test(val a: Pair<String, String>, val b: Pair<String, Long>)
    ConfigLoader().loadConfig<Test>("/test_pair.toml").shouldBeValid {
      it.a shouldBe Test("hello" to "world", "5" to 6)
    }
  }
})
