package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TripleDecoderTest : StringSpec({

  "triple should be decoded from string with 3 fields" {
    data class Test(val a: Triple<String, String, Boolean>, val b: Triple<String, Long, Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_triple.props")
    config shouldBe Test(Triple("hello", "world", true), Triple("5", 4L, 3))
  }
})
