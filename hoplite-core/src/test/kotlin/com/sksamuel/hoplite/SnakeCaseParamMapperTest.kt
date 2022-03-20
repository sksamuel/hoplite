package com.sksamuel.hoplite

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
        SnakeCaseParamMapper.map(kparam("foo")) shouldBe setOf("foo")
        SnakeCaseParamMapper.map(kparam("fooCamelCase")) shouldBe setOf("foo_camel_case")
        SnakeCaseParamMapper.map(kparam("foo_snake_case")) shouldBe setOf("foo_snake_case")
        SnakeCaseParamMapper.map(kparam("_underScore")) shouldBe setOf("_under_score")
        SnakeCaseParamMapper.map(kparam("TitleCase")) shouldBe setOf("title_case")
        SnakeCaseParamMapper.map(kparam("foo123")) shouldBe setOf("foo123")
        SnakeCaseParamMapper.map(kparam("foo123BarFaz")) shouldBe setOf("foo123_bar_faz")
        SnakeCaseParamMapper.map(kparam("myDSLClass")) shouldBe setOf("my_d_s_l_class")
      }
    }
  }
}
