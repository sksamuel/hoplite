package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class RegexDecoderTest : StringSpec({
  "regex decoded from TOML" {
    data class Test(val a: Regex)
    ConfigLoader().loadConfig<Test>("/test_regex.toml").shouldBeValid {
      it.a.a.toString() shouldBe ".*?".toRegex().toString()
    }
  }
})
