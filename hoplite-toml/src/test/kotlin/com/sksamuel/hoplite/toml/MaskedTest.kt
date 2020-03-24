package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Masked
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeTypeOf

class MaskedTest : StringSpec() {
  init {
    "support built in Masked type" {
      data class Test(val a: Masked)
      val test = ConfigLoader().loadConfigOrThrow<Test>("/masked.toml")
      test.a.shouldBeTypeOf<Masked>()
      test.a.value shouldBe "password"
      test.a.toString() shouldBe "****"
    }
  }
}
