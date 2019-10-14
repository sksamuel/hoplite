package com.sksamuel.hoplite

import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class KebabCaseParamMapperTest : StringSpec() {
  init {
    "mapping param to kebab case" {
      data class Config(val foo: String,
                        val fooCamelCase: String,
                        val foo_snake_case: String,
                        val _underScore: String,
                        val TitleCase: String,
                        val foo123: String,
                        val foo123BarFaz: String,
                        val myDSLClass: String)
      assertSoftly {
        KebabCaseParamMapper.name(Config::foo) shouldBe "foo"
        KebabCaseParamMapper.name(Config::fooCamelCase) shouldBe "foo-camel-case"
        KebabCaseParamMapper.name(Config::foo_snake_case) shouldBe "foo_snake_case"
        KebabCaseParamMapper.name(Config::_underScore) shouldBe "_under-score"
        KebabCaseParamMapper.name(Config::TitleCase) shouldBe "title-case"
        KebabCaseParamMapper.name(Config::foo123) shouldBe "foo123"
        KebabCaseParamMapper.name(Config::foo123BarFaz) shouldBe "foo123-bar-faz"
        KebabCaseParamMapper.name(Config::myDSLClass) shouldBe "my-d-s-l-class"
      }
    }
  }
}
