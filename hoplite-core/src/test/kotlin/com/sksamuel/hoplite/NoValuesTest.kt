package com.sksamuel.hoplite

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class NoValuesTest : FunSpec({

  test("ConfigLoader should return NoValues if all sources returned Undefined") {
    ConfigLoader().loadNode().getInvalidUnsafe() shouldBe ConfigFailure.UndefinedTree
  }

  test("ConfigLoader should return meaningful error if no sources return a value") {
    shouldThrowAny {
      ConfigLoader().loadNodeOrThrow()
    }.message shouldContain "The applied config was empty"
  }

  test("ConfigLoader should ignore no values when option is enabled") {

    data class NestedConfig(
      val nums: List<Int> = emptyList()
    )

    data class Test(
      val name: String = "a",
      val nested: NestedConfig = NestedConfig()
    )

    ConfigLoaderBuilder.default()
      .allowEmptyConfigFiles()
      .build()
      .loadConfigOrThrow<Test>()
      .name shouldBe "a"
  }
})
