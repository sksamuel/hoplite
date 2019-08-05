package com.sksamuel.hoplite.okhttp

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldBe
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class HttpUrlDecoderTest : StringSpec() {
  init {
    "http url decoder happy path" {
      data class Foo(val url: HttpUrl)
      ConfigLoader().loadConfigOrThrow<Foo>("/httpurl.yml") shouldBe Foo("http://github.com/sksamuel".toHttpUrlOrNull()!!)
    }
    "http url builder decoder happy path" {
      data class Foo(val url: HttpUrl.Builder)
      ConfigLoader().loadConfigOrThrow<Foo>("/httpurl.yml").url.build().toString() shouldBe "http://github.com/sksamuel"
    }
  }
}
