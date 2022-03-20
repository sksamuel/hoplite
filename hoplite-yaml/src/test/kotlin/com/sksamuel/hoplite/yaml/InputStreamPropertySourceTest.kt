package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.sources.InputStreamPropertySource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class InputStreamPropertySourceTest : StringSpec({
  "input stream decoding test" {

    data class Test(val a: Set<Long>, val b: Set<String>)

    val stream = javaClass.getResourceAsStream("/sets.yml")
    val config = ConfigLoaderBuilder.default()
      .addPropertySource(InputStreamPropertySource(stream, "yml", "yml input stream"))
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    config.a.shouldBeInstanceOf<HashSet<*>>()
  }
})
