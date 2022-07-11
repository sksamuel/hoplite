package com.sksamuel.hoplite

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class NoValuesTest : FunSpec({

  test("ConfigLoader should return NoValues if all sources returned Undefined") {
    ConfigLoader().loadNode().getInvalidUnsafe() shouldBe ConfigFailure.NoValues
  }

  test("ConfigLoader should return meaningful error if no sources return a value") {
    shouldThrowAny {
      ConfigLoader().loadNodeOrThrow()
    }.message shouldContain "Registered properties sources returned no config"
  }

  test("ConfigLoader should ignore no values when option is enabled") {

    data class NestedConfig(
      val nums: List<Int> = emptyList(),
    )

    data class Test(
      val name: String = "a",
      val nested: NestedConfig = NestedConfig(),
    )

    ConfigLoaderBuilder.default()
      .allowEmptyTree()
      .build()
      .loadConfigOrThrow<Test>()
      .name shouldBe "a"
  }
})
