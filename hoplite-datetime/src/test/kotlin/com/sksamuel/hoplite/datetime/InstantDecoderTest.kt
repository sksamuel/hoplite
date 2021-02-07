package com.sksamuel.hoplite.datetime

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant

class InstantDecoderTest : FunSpec() {
  init {
    test("test instant") {
      data class Config(val a: Instant)
      ConfigLoader().loadConfigOrThrow<Config>("/instant.props").a shouldBe Instant.fromEpochMilliseconds(1423131323423)
    }
    test("test iso instant") {
      data class Config(val a: Instant)
      ConfigLoader().loadConfigOrThrow<Config>("/instant_iso.props").a shouldBe Instant.parse("2007-12-03T10:15:30.00Z")
    }
  }
}
