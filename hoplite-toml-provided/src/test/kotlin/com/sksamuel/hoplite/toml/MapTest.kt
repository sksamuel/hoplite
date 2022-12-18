package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MapTest : StringSpec({
  "Map key contains dots" {
    data class CountEntry(val number: Int)
    data class Config(val count: Map<String, CountEntry>)

    val config = shouldNotThrowAny {
      ConfigLoader().loadConfigOrThrow<Config>("/key_with_dots_in_map.toml")
    }
    config shouldBe Config(
      count = mapOf(
        "one" to CountEntry(1),
        "twenty.two" to CountEntry(22),
        "nine.one.one" to CountEntry(911),
        "one.hundred.and.ten" to CountEntry(110)
      )
    )
  }
})
