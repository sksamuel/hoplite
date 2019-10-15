package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

class SortedSetDecoderTest : StringSpec({
  "sorted set decoded from json" {
    data class Test(val a: SortedSet<Long>, val b: SortedSet<String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/sets.json")
    config shouldBe Test(sortedSetOf(1, 2, 3), sortedSetOf("1", "2"))
  }
})
