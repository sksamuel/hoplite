package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FloatingPointTest : FunSpec() {
  init {
    test("should support whole numbers for floats and doubles") {

      data class Conf(val a: Float, val b: Double)

      val config = ConfigLoader().loadConfigOrThrow<Conf>("/floats.conf")
      config.a shouldBe 5.0
      config.b shouldBe 5.0
    }
  }
}
