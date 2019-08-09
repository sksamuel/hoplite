package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.Period

class PeriodTest : StringSpec() {
  init {
    "periods happy path" {
      data class Test(val a: Period,
                      val b: Period,
                      val c: Period,
                      val d: Period,
                      val e: Period,
                      val f: Period,
                      val g: Period)
      ConfigLoader().loadConfigOrThrow<Test>("/period.toml") shouldBe Test(
        Period.ofDays(7),
        Period.ofDays(2),
        Period.ofYears(3),
        Period.ofMonths(4),
        Period.ofYears(3),
        Period.ofDays(63),
        Period.ofDays(7))
    }
  }
}
