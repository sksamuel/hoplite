package com.sksamuel.hoplite.yaml

import arrow.core.Invalid
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class NullTests : StringSpec() {
  init {

    "null value provided for nullable field" {
      data class Test(val a: String?, val b: Double?)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/test_nulls.yml")
      config shouldBe Test(null, null)
    }

    "null value provided for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_nulls.yml").shouldBeInstanceOf<Invalid<*>>()
    }

    "undefined value for nullable field" {
      data class Test(val a: String?, val b: Double?)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/test_undefined.yml")
      config shouldBe Test(null, null)
    }

    "undefined value for non-nullable field" {
      data class Test(val a: String, val b: Double)
      ConfigLoader().loadConfig<Test>("/test_undefined.yml").shouldBeInstanceOf<Invalid<*>>()
    }
  }
}
