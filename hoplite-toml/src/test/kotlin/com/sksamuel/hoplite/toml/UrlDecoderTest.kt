package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.URI
import java.net.URL

class UrlDecoderTest : StringSpec({
  "Urls decoded from TOML" {
    data class Test(val a: URL, val b: URI)
    ConfigLoader().loadConfig<Test>("/test_urls.toml").shouldBeValid {
      it.a shouldBe Test(URL("http://www.google.com?search=noprivacy"),
        URI.create("http://www.google.com?search=noprivacy"))
    }
  }
})
