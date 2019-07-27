package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TruthyTest : StringSpec({
  "yes/no values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean, val e: Boolean, val f: Boolean)
    ConfigLoader(Json).loadConfig<Foo>("/truthy_yesno.json").shouldBeValid {
      it.a.a shouldBe true
      it.a.b shouldBe false
      it.a.c shouldBe true
      it.a.d shouldBe false
      it.a.e shouldBe true
      it.a.f shouldBe false
    }
  }
  "1/0 values" {
    data class Foo(val a: Boolean, val b: Boolean)
    ConfigLoader(Json).loadConfig<Foo>("/truthy_10.json").shouldBeValid {
      it.a.a shouldBe true
      it.a.b shouldBe false
    }
  }
  "T/F values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean)
    ConfigLoader(Json).loadConfig<Foo>("/truthy_TF.json").shouldBeValid {
      it.a.a shouldBe true
      it.a.b shouldBe true
      it.a.c shouldBe false
      it.a.d shouldBe false
    }
  }
})