package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PairDecoderTest : StringSpec({
  "pair decoded from toml" {
    data class Test(val a: Pair<String, String>, val b: Pair<String, Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_pair.toml")
    config shouldBe Test("hello" to "world", "5" to 6)
  }
})
