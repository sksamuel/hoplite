package com.sksamuel.hoplite

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SnakeCaseKeyMapperTest : StringSpec() {
  init {
    "mapping snake case to camel case" {
      SnakeCaseKeyMapper.map("") shouldBe ""
      SnakeCaseKeyMapper.map("a_") shouldBe "a"
      SnakeCaseKeyMapper.map("_a") shouldBe "A"
      SnakeCaseKeyMapper.map("hello_world") shouldBe "helloWorld"
      SnakeCaseKeyMapper.map("helloworld") shouldBe "helloworld"
      SnakeCaseKeyMapper.map("hello_World") shouldBe "helloWorld"
      SnakeCaseKeyMapper.map("Hello_World") shouldBe "helloWorld"
      SnakeCaseKeyMapper.map("Hello_World_A_bbb") shouldBe "helloWorldABbb"
      SnakeCaseKeyMapper.map("Hello_World11_AA_22") shouldBe "helloWorld11AA22"
    }
  }
}
