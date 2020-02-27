package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

inline class Username(val value: String)

class NullTests : StringSpec() {
  init {

    "null value provided for nullable field" {
      data class Test(val a: String?, val b: Double?)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/test_nulls.yml")
      config shouldBe Test(null, null)
    }

    "null value provided for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_nulls.yml").shouldBeInstanceOf<Validated.Invalid<*>>()
    }

    "undefined value for nullable field" {
      data class Test(val a: String?, val b: Double?)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/test_undefined.yml")
      config shouldBe Test(null, null)
    }

    "undefined value for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_undefined.yml").shouldBeInstanceOf<Validated.Invalid<*>>()
    }

    "null value provided for inline class" {
      data class Test(val user1: Username?, val user2: Username?, val user3: Username?)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/test_null_inline.yml")
      config shouldBe Test(Username("sammy"), null, null)
    }
  }
}
