package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AnchorAliasTest : FunSpec() {
  init {

    test("should support anchors and aliases") {
      data class Hand(val name: String, val weight: Int)
      data class Test(val objects: List<String>, val lefthand: Hand, val righthand: Hand)

      val config = ConfigLoader().loadConfigOrThrow<Test>("/anchor.yml")
      config shouldBe Test(
        listOf("Apple", "Beachball", "Cartoon", "Duckface", "Apple"),
        Hand("The Bastard Sword of Eowyn", 30),
        Hand("The Bastard Sword of Eowyn", 30)
      )
    }
  }
}
