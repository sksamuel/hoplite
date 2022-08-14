package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

class LocaleDecoderTest : StringSpec({
  "Locale decoded from TOML" {
    data class Test(val a: Locale, val b: Locale)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_locale.toml")
    config.a shouldBe Locale.forLanguageTag("en_GB")
    config.b shouldBe Locale.forLanguageTag("en_GB")
  }
})
