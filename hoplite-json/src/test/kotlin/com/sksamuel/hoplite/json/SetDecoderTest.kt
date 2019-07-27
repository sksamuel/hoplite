package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SetDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val a: Set<Long>, val b: Set<String>)
    ConfigLoader().loadConfig<Test>("/sets.json").shouldBeValid {
      it.a shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    }
  }
})