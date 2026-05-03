package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.List
import io.vavr.kotlin.list

class ListDecoderTest : FunSpec({

  test("List<String> decoded from yaml") {
    data class Test(val strings: List<String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_list.yml")
    config shouldBe Test(list("test1", "test2", "test3"))
  }

  // Per-element decoders must be invoked with the element KType, not the outer
  // List<T> KType, otherwise nested decoders that read type.arguments break.
  test("List<Map<String, Int>> passes element type to map decoder") {
    data class Test(val items: List<Map<String, Int>>)

    val config = ConfigLoaderBuilder.default()
      .addPropertySource(
        YamlPropertySource(
          """
            items:
              - a: 1
                b: 2
              - c: 3
          """
        )
      )
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(list(mapOf("a" to 1, "b" to 2), mapOf("c" to 3)))
  }

  test("List<DataClass> passes element type to data class decoder") {
    data class Item(val name: String, val score: Int)
    data class Test(val items: List<Item>)

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

    config shouldBe Test(list(Item("alice", 1), Item("bob", 2)))
  }

})
