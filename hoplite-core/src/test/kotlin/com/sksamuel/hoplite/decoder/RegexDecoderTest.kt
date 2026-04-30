package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RegexDecoderTest : StringSpec({

  "regex decoder should decode strings into a Regex" {
    data class Test(val a: Regex)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_regex_valid.props")
    config.a.pattern shouldBe "^foo[0-9]+$"
  }

  "regex decoder should report invalid patterns as a config failure rather than throwing" {
    data class Test(val a: Regex)

    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Test>("/test_regex_invalid.props")
    }
  }
})
