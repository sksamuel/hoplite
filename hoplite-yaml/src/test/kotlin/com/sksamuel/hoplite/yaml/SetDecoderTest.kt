package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class SetDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val a: Set<Long>, val b: Set<String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/sets.yml")
    config shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    config.a.shouldBeInstanceOf<HashSet<*>>()
  }
})
