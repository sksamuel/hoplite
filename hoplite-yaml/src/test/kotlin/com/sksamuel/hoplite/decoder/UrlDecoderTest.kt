package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.net.URI
import java.net.URL

class UrlDecoderTest : StringSpec({
  "Urls decoded from YAML" {
    data class Test(val a: URL, val b: URI)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_urls.yml")
    config shouldBe Test(
      URL("http://www.google.com?search=noprivacy"),
      URI.create("http://www.google.com?search=noprivacy")
    )
  }
})
