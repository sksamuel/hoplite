package com.sksamuel.hoplite.secrets

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultSecretStrengthAnalyzerTest : FunSpec({

  test("should detect lack of digits") {
    DefaultSecretStrengthAnalyzer.strength("fooooooooooooooo") shouldBe SecretStrength.Weak("Does not contain a digit")
  }

  test("should detect lack of special chars") {
    DefaultSecretStrengthAnalyzer.strength("123foo_-2343242424") shouldBe SecretStrength.Weak("Does not contain a non-alphanumeric character")
  }

  test("length check") {
    DefaultSecretStrengthAnalyzer.strength("123abc%%$$") shouldBe SecretStrength.Weak("Too short")
  }

  test("happy path") {
    DefaultSecretStrengthAnalyzer.strength("foo123%%%sSDQWE") shouldBe SecretStrength.Strong
  }
})
