package com.sksamuel.hoplite

import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.reflect.KParameter

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

      fun kparam(name: String): KParameter = Config::class.constructors.first().parameters.find { it.name == name }!!

      assertSoftly {
        SnakeCaseParamMapper.map(kparam("foo")) shouldBe "foo"
        SnakeCaseParamMapper.map(kparam("fooCamelCase")) shouldBe "foo_camel_case"
        SnakeCaseParamMapper.map(kparam("foo_snake_case")) shouldBe "foo_snake_case"
        SnakeCaseParamMapper.map(kparam("_underScore")) shouldBe "_under_score"
        SnakeCaseParamMapper.map(kparam("TitleCase")) shouldBe "title_case"
        SnakeCaseParamMapper.map(kparam("foo123")) shouldBe "foo123"
        SnakeCaseParamMapper.map(kparam("foo123BarFaz")) shouldBe "foo123_bar_faz"
        SnakeCaseParamMapper.map(kparam("myDSLClass")) shouldBe "my_d_s_l_class"
      }
    }
  }
}
