package com.sksamuel.hoplite

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KParameter

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

      fun kparam(name: String): KParameter = Config::class.constructors.first().parameters.find { it.name == name }!!

      assertSoftly {

        KebabCaseParamMapper.map(kparam("foo")) shouldBe setOf("foo")
        KebabCaseParamMapper.map(kparam("fooCamelCase")) shouldBe setOf("foo-camel-case")
        KebabCaseParamMapper.map(kparam("foo_snake_case")) shouldBe setOf("foo_snake_case")
        KebabCaseParamMapper.map(kparam("_underScore")) shouldBe setOf("_under-score")
        KebabCaseParamMapper.map(kparam("TitleCase")) shouldBe setOf("title-case")
        KebabCaseParamMapper.map(kparam("foo123")) shouldBe setOf("foo123")
        KebabCaseParamMapper.map(kparam("foo123BarFaz")) shouldBe setOf("foo123-bar-faz")
        KebabCaseParamMapper.map(kparam("myDSLClass")) shouldBe setOf("my-d-s-l-class")
      }
    }
  }
}
