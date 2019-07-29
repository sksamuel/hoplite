package com.sksamuel.hoplite.preprocessor

import io.kotlintest.matchers.string.shouldHaveLength
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class UUIDPreprocessorTest : StringSpec() {
  init {
    "should replace \$UUID()" {
      UUIDPreprocessor.process("hello \$uuid()") shouldHaveLength 42
      UUIDPreprocessor.process("\$uuid()") shouldHaveLength 36
      UUIDPreprocessor.process("hello \$uuid") shouldBe "hello \$uuid"
    }
  }
}
