package com.sksamuel.hoplite

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class KebabCaseKeyMapperTest : StringSpec() {
  init {
    "mapping kebab case to camel case" {
      KebabCaseKeyMapper.map("hello-world") shouldBe "helloWorld"
      KebabCaseKeyMapper.map("helloworld") shouldBe "helloworld"
      KebabCaseKeyMapper.map("hello-World") shouldBe "helloWorld"
      KebabCaseKeyMapper.map("Hello-World-2") shouldBe "helloWorld2"
      KebabCaseKeyMapper.map("Hello-World-a-b") shouldBe "helloWorldAB"
      KebabCaseKeyMapper.map("Hello-World-a-b-c-dee") shouldBe "helloWorldABCDee"
    }
  }
}
