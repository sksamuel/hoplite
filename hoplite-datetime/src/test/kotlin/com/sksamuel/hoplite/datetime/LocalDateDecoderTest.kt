package com.sksamuel.hoplite.datetime

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class LocalDateDecoderTest : FunSpec() {
  init {
    test("test local date") {
      data class Config(val a: LocalDate)
      ConfigLoader().loadConfigOrThrow<Config>("/localdate.props").a shouldBe LocalDate.parse("1974-08-10")
    }
  }
}
