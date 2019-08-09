package com.sksamuel.hoplite

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class DashCaseKeyMapperTest : StringSpec() {
  init {
    "mapping snake case to camel case" {
      DashCaseKeyMapper.map("hello-world") shouldBe "helloWorld"
      DashCaseKeyMapper.map("helloworld") shouldBe "helloworld"
      DashCaseKeyMapper.map("hello-World") shouldBe "helloWorld"
      DashCaseKeyMapper.map("Hello-World") shouldBe "helloWorld"
    }
  }
}
