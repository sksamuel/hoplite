package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NodeParserTest : FunSpec({

  test("NodeParser should return NoValues if all sources returned Undefined") {
    ConfigLoader().loadNode().getInvalidUnsafe() shouldBe ConfigFailure.NoValues
  }

})
