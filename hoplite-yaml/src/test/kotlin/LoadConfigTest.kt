package com.sksamuel.hoplite.yaml

import arrow.data.NonEmptyList
import com.sksamuel.hoplite.ConfigFailure
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class LoadConfigTest : FunSpec({

  test("return failure for missing resource") {
    loadConfig<String>("/missing.yml").shouldBeInvalid {
      it.e shouldBe NonEmptyList.just(ConfigFailure("Could not find resource /missing.yml"))
    }
  }

  test("loading basic data class with primitive fields") {
    data class Test(val a: String, val b: Int, val c: Long, val d: Boolean, val e: Float, val f: Double)
    loadConfig<Test>("/test1.yml").shouldBeValid {
      it.a shouldBe Test(a = "Sammy", b = 1, c = 12312313123, d = true, e = 10.4F, f = 5646.54)
    }
  }

})