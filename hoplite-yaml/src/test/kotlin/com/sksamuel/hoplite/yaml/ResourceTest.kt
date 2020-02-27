package com.sksamuel.hoplite.yaml

import arrow.core.Invalid
import arrow.core.NonEmptyList
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.FunSpec

class ResourceTest : FunSpec({

  test("return failure for missing resource") {
    data class Foo(val a: String)

    val e = ConfigLoader().loadConfig<Foo>("/missing.yml") as Invalid<ConfigFailure>
    e.e shouldBe ConfigFailure.MultipleFailures(NonEmptyList.just(ConfigFailure.UnknownSource("/missing.yml")))
  }

  test("support fallback") {
    data class Test(val a: String, val b: String, val c: String, val d: String)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/fallback_1.yml", "/fallback_2.yml", "/fallback_3.yml")
    config.a shouldBe "foo"
    config.b shouldBe "voo"
    config.c shouldBe "woo"
    config.d shouldBe "roo"
  }
})
