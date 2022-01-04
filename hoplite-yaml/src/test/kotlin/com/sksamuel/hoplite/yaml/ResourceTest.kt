package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigFilePropertySource
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ResourceTest : FunSpec({

  test("return failure for missing resource") {
    data class Foo(val a: String)

    val e = ConfigLoader().loadConfig<Foo>("/missing.yml") as Validated.Invalid<ConfigFailure>
    e.error.description() shouldBe """Could not find config file /missing.yml"""
  }

  test("do not return failure for optional file property source") {
    data class Foo(val a: String)

    run {
      ConfigLoader.Builder()
        .addPropertySource(ConfigFilePropertySource.optionalResource("/missing.yml"))
        .build()
        .loadConfig<Foo>("/basic.yml").getUnsafe().a
    } shouldBe "hello"
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
