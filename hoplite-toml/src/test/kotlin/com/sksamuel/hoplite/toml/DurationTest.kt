package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DurationTest : StringSpec() {
  init {

    "java durations should be parsable from milli longs" {
      data class Test(val a: java.time.Duration)
      ConfigLoader().loadConfigOrThrow<Test>("/duration_as_longs.toml").a shouldBe
        java.time.Duration.ofMillis(123123213123)
    }

    "kotlin durations should be parsable from milli longs" {
      data class Test(val a: kotlin.time.Duration)
      ConfigLoader().loadConfigOrThrow<Test>("/duration_as_longs.toml").a shouldBe
        123123213123.milliseconds
    }
  }
}
