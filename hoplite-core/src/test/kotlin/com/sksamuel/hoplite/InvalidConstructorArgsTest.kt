package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class InvalidConstructorArgsTest : FunSpec() {
  init {
    test("invalid constructor args") {
      // linked hash set is not supported, so this should cause a normal set to be picked up
      // which will in turn cause the constructor to baulk
      data class Config(val e: LinkedHashSet<String>)
      val e = ConfigLoader().loadConfig<Config>("/basic.props")
      e as Validated.Invalid<ConfigFailure>
      e.error.description() shouldBe "Could not instantiate class com.sksamuel.hoplite.InvalidConstructorArgsTest\$1\$Config from args [java.util.Collections\$SingletonSet]: Expected args are [class java.util.LinkedHashSet]"
    }
  }
}
