package com.sksamuel.hoplite.preprocessor

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PropsPreprocessorTest : StringSpec() {
  init {
    "should replace props from file" {
      PropsPreprocessor("/sample.properties").process("hello \${git.branch}") shouldBe "hello master"
      PropsPreprocessor("/sample.properties").process("hello \${git.commit.id} on branch \${git.branch}") shouldBe
        "hello 47854c215b57b69871d316ea694e8278e998c176 on branch master"
    }
  }
}
