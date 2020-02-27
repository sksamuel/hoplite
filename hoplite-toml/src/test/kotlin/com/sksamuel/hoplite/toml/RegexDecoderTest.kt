package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class RegexDecoderTest : StringSpec({
  "regex decoded from TOML" {
    data class Test(val a: Regex)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_regex.toml")
    config.a.toString() shouldBe ".*?".toRegex().toString()
  }
})
