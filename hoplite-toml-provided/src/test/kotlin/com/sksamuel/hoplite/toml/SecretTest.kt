package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Secret
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class SecretTest : StringSpec() {
  init {
    "support built in Secret type" {
      data class Test(val a: Secret)

      val test = ConfigLoader().loadConfigOrThrow<Test>("/masked.toml")
      test.a.shouldBeTypeOf<Secret>()
      test.a.value shouldBe "password"
      test.a.toString() shouldBe "****"
    }
  }
}
