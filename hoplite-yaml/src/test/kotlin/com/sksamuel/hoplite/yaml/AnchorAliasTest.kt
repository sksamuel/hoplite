package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AnchorAliasTest : FunSpec() {
  init {

    test("should support anchors and aliases") {
      data class Hand(val name: String, val weight: Int)
      data class Test(val objects: List<String>, val lefthand: Hand, val righthand: Hand)

      val config = ConfigLoader().loadConfigOrThrow<Test>(
        "/anchor.yml",
        "/anchor-merge-base.yml"
      )

      config shouldBe Test(
        listOf("Apple", "Beachball", "Cartoon", "Duckface", "Apple"),
        Hand("The Bastard Sword of Eowyn", 30),
        Hand("The Bastard Sword of Eowyn", 30)
      )
    }

    test("should support anchors and aliases on sequences") {
      data class Test(val defaults: List<String>, val other: List<String>)

      val yaml = """
        defaults: &d
          - a
          - b
        other: *d
      """.trimIndent()

      val config = ConfigLoader.builder()
        .addPropertySource(PropertySource.string(yaml, "yml"))
        .build()
        .loadConfigOrThrow<Test>()

      config shouldBe Test(listOf("a", "b"), listOf("a", "b"))
    }
  }
}
