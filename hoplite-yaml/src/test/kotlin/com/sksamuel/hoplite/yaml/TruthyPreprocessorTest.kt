package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.yaml.com.sksamuel.hoplite.yaml.Yaml
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TruthyPreprocessorTest : StringSpec({
  "yes/no values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean, val e: Boolean, val f: Boolean)
    ConfigLoader(Yaml).loadConfig<Foo>("/truthy_yesno.yml").shouldBeValid {
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
    ConfigLoader(Yaml).loadConfig<Foo>("/truthy_10.yml").shouldBeValid {
      it.a.a shouldBe true
      it.a.b shouldBe false
    }
  }
  "T/F values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean)
    ConfigLoader(Yaml).loadConfig<Foo>("/truthy_TF.yml").shouldBeValid {
      it.a.a shouldBe true
      it.a.b shouldBe true
      it.a.c shouldBe false
      it.a.d shouldBe false
    }
  }
})