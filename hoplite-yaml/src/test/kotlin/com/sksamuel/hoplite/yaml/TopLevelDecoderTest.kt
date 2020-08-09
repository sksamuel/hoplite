package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TopLevelDecoderTest : StringSpec({

  "should be able to decode top level List" {
    val config = ConfigLoader().loadConfigOrThrow<List<TopLevel>>("/top_level_list.yml")
    config shouldBe ""
  }
})

data class TopLevel(val a: String, val b: Int)
