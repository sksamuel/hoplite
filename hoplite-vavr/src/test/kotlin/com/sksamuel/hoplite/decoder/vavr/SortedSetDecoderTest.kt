package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.SortedSet
import io.vavr.collection.TreeSet
import io.vavr.kotlin.treeSet

data class VavrSortedSetItem(val name: String, val score: Int) : Comparable<VavrSortedSetItem> {
  override fun compareTo(other: VavrSortedSetItem): Int = name.compareTo(other.name)
}

class SortedSetDecoderTest : FunSpec({

  test("SortedSet<Int> decoded from yaml") {
    data class Test(val numbers: SortedSet<Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_set.yml")
    config shouldBe Test(treeSet(3, 2, 1))
  }

  // Per-element decoders must be invoked with the element KType, not the outer
  // SortedSet<T> KType — DataClassDecoder reads type.classifier.
  test("SortedSet<DataClass> passes element type to data class decoder") {
    data class Test(val items: SortedSet<VavrSortedSetItem>)

    val config = ConfigLoaderBuilder.default()
      .addPropertySource(
        YamlPropertySource(
          """
            items:
              - name: alice
                score: 1
              - name: bob
                score: 2
          """
        )
      )
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(TreeSet.of(VavrSortedSetItem("alice", 1), VavrSortedSetItem("bob", 2)))
  }

})
