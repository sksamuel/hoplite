package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.InputStreamPropertySource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class InputStreamPropertySourceTest : StringSpec({
  "input stream decoding test" {

    data class Test(val a: Set<Long>, val b: Set<String>)

    val stream = javaClass.getResourceAsStream("/sets.yml")
    val config = ConfigLoader.Builder()
      .addPropertySource(InputStreamPropertySource(stream, "yml"))
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    config.a.shouldBeInstanceOf<HashSet<*>>()
  }
})
