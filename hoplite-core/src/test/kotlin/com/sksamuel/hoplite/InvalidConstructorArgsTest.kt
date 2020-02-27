package com.sksamuel.hoplite

import arrow.core.Invalid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class InvalidConstructorArgsTest : FunSpec() {
  init {
    test("invalid constructor args") {
      // linked hash set is not supported, so this should cause a normal set to be picked up
      // which will in turn cause the constructor to baulk
      data class Config(val e: LinkedHashSet<String>)
      val e = ConfigLoader().loadConfig<Config>("/basic.props")
      e as Invalid<ConfigFailure>
      e.e.description() shouldBe "Could not instantiate class com.sksamuel.hoplite.InvalidConstructorArgsTest\$1\$Config from args [java.util.Collections\$SingletonSet]: Expected args are [class java.util.LinkedHashSet]"
    }
  }
}
