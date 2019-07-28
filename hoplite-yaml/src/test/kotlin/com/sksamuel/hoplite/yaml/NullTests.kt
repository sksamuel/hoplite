package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class NullTests : StringSpec() {
  init {

    "null value provided for nullable field" {
      data class Test(val a: String?, val b: Double?)
      ConfigLoader().loadConfig<Test>("/test_nulls.yml").shouldBeValid {
        it.a shouldBe Test(null, null)
      }
    }

    "null value provided for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_nulls.yml").shouldBeInvalid()
    }

    "undefined value for nullable field" {
      data class Test(val a: String?, val b: Double?)
      ConfigLoader().loadConfig<Test>("/test_undefined.yml").shouldBeValid {
        it.a shouldBe Test(null, null)
      }
    }

    "undefined value for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_undefined.yml").shouldBeInvalid()
    }
  }
}
