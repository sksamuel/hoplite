package com.sksamuel.hoplite

import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SnakeCaseParamMapperTest : StringSpec() {
  init {
    "mapping param to snake case" {
      data class Config(val foo: String,
                        val fooCamelCase: String,
                        val foo_snake_case: String,
                        val _underScore: String,
                        val TitleCase: String,
                        val foo123: String,
                        val foo123BarFaz: String,
                        val myDSLClass: String)
      assertSoftly {
        SnakeCaseParamMapper.name(Config::foo) shouldBe "foo"
        SnakeCaseParamMapper.name(Config::fooCamelCase) shouldBe "foo_camel_case"
        SnakeCaseParamMapper.name(Config::foo_snake_case) shouldBe "foo_snake_case"
        SnakeCaseParamMapper.name(Config::_underScore) shouldBe "_under_score"
        SnakeCaseParamMapper.name(Config::TitleCase) shouldBe "title_case"
        SnakeCaseParamMapper.name(Config::foo123) shouldBe "foo123"
        SnakeCaseParamMapper.name(Config::foo123BarFaz) shouldBe "foo123_bar_faz"
        SnakeCaseParamMapper.name(Config::myDSLClass) shouldBe "my_d_s_l_class"
      }
    }
  }
}
