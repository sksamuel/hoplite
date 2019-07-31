package com.sksamuel.hoplite.props

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TripleDecoderTest : StringSpec() {
  init {
    "triple should be decoded from string with 3 fields" {
      data class Test(val a: Triple<String, String, Boolean>, val b: Triple<String, Long, Int>)
      ConfigLoader().loadConfig<Test>("/test_triple.props").shouldBeValid {
        it.a shouldBe Test(Triple("hello", "world", true), Triple("5", 4L, 3))
      }
    }
  }
}
