package com.sksamuel.hoplite

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SnakeCaseKeyMapperTest : StringSpec() {
  init {
    "mapping snake case to camel case" {
      SnakeCaseKeyMapper.map("hello_world") shouldBe "helloWorld"
      SnakeCaseKeyMapper.map("helloworld") shouldBe "helloworld"
      SnakeCaseKeyMapper.map("hello_World") shouldBe "helloWorld"
      SnakeCaseKeyMapper.map("Hello_World") shouldBe "helloWorld"
    }
  }
}
