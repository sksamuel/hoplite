package com.sksamuel.hoplite.datetime

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LocalDateTimeDecoderTest : FunSpec() {
  init {
    test("test local date time") {
      println(Instant.fromEpochMilliseconds(145354234234).toLocalDateTime(TimeZone.UTC).toString())
      data class Config(val a: LocalDateTime)
      ConfigLoader().loadConfigOrThrow<Config>("/localdatetime.props").a shouldBe LocalDateTime.parse("1974-08-10T08:10:34.234")
    }
  }
}
