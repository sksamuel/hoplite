package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TruthyTest : StringSpec({
  "yes/no values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean, val e: Boolean, val f: Boolean)

    val config = ConfigLoader().loadConfigOrThrow<Foo>("/truthy_yesno.yml")
    config.a shouldBe true
    config.b shouldBe false
    config.c shouldBe true
    config.d shouldBe false
    config.e shouldBe true
    config.f shouldBe false
  }
  "1/0 values" {
    data class Foo(val a: Boolean, val b: Boolean)

    val config = ConfigLoader().loadConfigOrThrow<Foo>("/truthy_10.yml")
    config.a shouldBe true
    config.b shouldBe false
  }
  "T/F values" {
    data class Foo(val a: Boolean, val b: Boolean, val c: Boolean, val d: Boolean)

    val config = ConfigLoader().loadConfigOrThrow<Foo>("/truthy_TF.yml")
    config.a shouldBe true
    config.b shouldBe true
    config.c shouldBe false
    config.d shouldBe false
  }
})
