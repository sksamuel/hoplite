package com.sksamuel.hoplite

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KParameter

class CaseMapperAcronymsTest : FunSpec({

  // Lock in the Spring/Jackson convention for acronyms in parameter names. The previous
  // implementation inserted a separator before every uppercase letter, so `URLConfig` produced
  // `u_r_l_config` and `myURL` produced `my_u_r_l`. Acronyms should now be kept as a single
  // segment, matching what users typically expect.
  data class Config(
    val URLConfig: String,
    val myURL: String,
    val XMLHttpRequest: String,
    val parseHTTPRequest: String,
    val URL: String,
    val A: String,
    val ABC: String,
    val httpEndpoint: String,
  )

  val constructor = Config::class.constructors.first()
  fun kparam(name: String): KParameter = constructor.parameters.find { it.name == name }!!

  test("SnakeCaseParamMapper keeps consecutive uppercase letters together as acronyms") {
    assertSoftly {
      SnakeCaseParamMapper.map(kparam("URLConfig"), constructor, Config::class) shouldBe setOf("url_config")
      SnakeCaseParamMapper.map(kparam("myURL"), constructor, Config::class) shouldBe setOf("my_url")
      SnakeCaseParamMapper.map(kparam("XMLHttpRequest"), constructor, Config::class) shouldBe setOf("xml_http_request")
      SnakeCaseParamMapper.map(kparam("parseHTTPRequest"), constructor, Config::class) shouldBe setOf("parse_http_request")
      SnakeCaseParamMapper.map(kparam("URL"), constructor, Config::class) shouldBe setOf("url")
      SnakeCaseParamMapper.map(kparam("A"), constructor, Config::class) shouldBe setOf("a")
      SnakeCaseParamMapper.map(kparam("ABC"), constructor, Config::class) shouldBe setOf("abc")
      SnakeCaseParamMapper.map(kparam("httpEndpoint"), constructor, Config::class) shouldBe setOf("http_endpoint")
    }
  }

  test("KebabCaseParamMapper keeps consecutive uppercase letters together as acronyms") {
    assertSoftly {
      KebabCaseParamMapper.map(kparam("URLConfig"), constructor, Config::class) shouldBe setOf("url-config")
      KebabCaseParamMapper.map(kparam("myURL"), constructor, Config::class) shouldBe setOf("my-url")
      KebabCaseParamMapper.map(kparam("XMLHttpRequest"), constructor, Config::class) shouldBe setOf("xml-http-request")
      KebabCaseParamMapper.map(kparam("parseHTTPRequest"), constructor, Config::class) shouldBe setOf("parse-http-request")
      KebabCaseParamMapper.map(kparam("URL"), constructor, Config::class) shouldBe setOf("url")
      KebabCaseParamMapper.map(kparam("A"), constructor, Config::class) shouldBe setOf("a")
      KebabCaseParamMapper.map(kparam("ABC"), constructor, Config::class) shouldBe setOf("abc")
      KebabCaseParamMapper.map(kparam("httpEndpoint"), constructor, Config::class) shouldBe setOf("http-endpoint")
    }
  }
})
