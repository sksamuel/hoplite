package com.sksamuel.hoplite.yaml

import arrow.data.NonEmptyList
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class ResourceTest : FunSpec({

  test("return failure for missing resource") {
    data class Foo(val a: String)
    ConfigLoader().loadConfig<Foo>("/missing.yml").shouldBeInvalid {
      it.e shouldBe NonEmptyList.just(ConfigFailure("Could not find resource /missing.yml"))
    }
  }

  test("support fallback") {
    data class Test(val a: String, val b: String, val c: String, val d: String)
    ConfigLoader().loadConfig<Test>("/fallback_1.yml", "/fallback_2.yml", "/fallback_3.yml").shouldBeValid {
      it.a.a shouldBe "foo"
      it.a.b shouldBe "voo"
      it.a.c shouldBe "woo"
      it.a.d shouldBe "roo"
    }
  }
})
