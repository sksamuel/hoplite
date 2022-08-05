package com.sksamuel.hoplite

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KParameter

class SnakeCaseParamMapperTest : StringSpec() {
  init {

    "mapping param to snake case" {
      data class Config(
        val foo: String,
        val fooCamelCase: String,
        val foo_snake_case: String,
        val _underScore: String,
        val TitleCase: String,
        val foo123: String,
        val foo123BarFaz: String,
        val myDSLClass: String
      )

      val constructor = Config::class.constructors.first()
      fun kparam(name: String): KParameter = constructor.parameters.find { it.name == name }!!

      assertSoftly {
        SnakeCaseParamMapper.map(kparam("foo"), constructor, Config::class) shouldBe setOf("foo")
        SnakeCaseParamMapper.map(kparam("fooCamelCase"), constructor, Config::class) shouldBe setOf("foo_camel_case")
        SnakeCaseParamMapper.map(kparam("foo_snake_case"), constructor, Config::class) shouldBe setOf("foo_snake_case")
        SnakeCaseParamMapper.map(kparam("_underScore"), constructor, Config::class) shouldBe setOf("_under_score")
        SnakeCaseParamMapper.map(kparam("TitleCase"), constructor, Config::class) shouldBe setOf("title_case")
        SnakeCaseParamMapper.map(kparam("foo123"), constructor, Config::class) shouldBe setOf("foo123", "foo_123")
        SnakeCaseParamMapper.map(kparam("foo123BarFaz"), constructor, Config::class) shouldBe setOf("foo123_bar_faz")
        SnakeCaseParamMapper.map(kparam("myDSLClass"), constructor, Config::class) shouldBe setOf("my_d_s_l_class")
      }
    }
  }
}
