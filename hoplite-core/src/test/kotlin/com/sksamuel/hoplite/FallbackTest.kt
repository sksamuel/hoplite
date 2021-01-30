package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FallbackTest : FunSpec({

  test("fallback should work with two maps at immediate depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos),
        "b" to StringNode("bar", Pos.NoPos)
      ),
      Pos.NoPos
    )

    val node2 = MapNode(
      mapOf(
        "a" to StringNode("faz", Pos.NoPos),
        "c" to StringNode("baz", Pos.NoPos)
      ),
      Pos.NoPos
    )

    val f = node1.merge(node2)
    f["a"] shouldBe StringNode("foo", Pos.NoPos)
    f["b"] shouldBe StringNode("bar", Pos.NoPos)
    f["c"] shouldBe StringNode("baz", Pos.NoPos)

    val g = node2.merge(node1)
    g["a"] shouldBe StringNode("faz", Pos.NoPos)
    g["b"] shouldBe StringNode("bar", Pos.NoPos)
    g["c"] shouldBe StringNode("baz", Pos.NoPos)
  }

  test("fallback should work with two maps at arbitrary depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos),
        "b" to MapNode(
          mapOf(
            "j" to StringNode("jen", Pos.NoPos),
            "k" to StringNode("ken", Pos.NoPos)
          ),
          Pos.NoPos
        )
      ),
      Pos.NoPos
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.NoPos)
          ),
          Pos.NoPos
        ),
        "c" to StringNode("baz", Pos.NoPos)
      ),
      Pos.NoPos
    )

    val f = node1.merge(node2)
    f["a"] shouldBe StringNode("foo", Pos.NoPos)
    f["b"]["j"] shouldBe StringNode("jen", Pos.NoPos)
    f["b"]["k"] shouldBe StringNode("ken", Pos.NoPos)
    f["c"] shouldBe StringNode("baz", Pos.NoPos)

    val g = node2.merge(node1)
    g["a"] shouldBe StringNode("foo", Pos.NoPos)
    g["b"]["j"] shouldBe StringNode("jen", Pos.NoPos)
    g["b"]["k"] shouldBe StringNode("kez", Pos.NoPos)
    g["c"] shouldBe StringNode("baz", Pos.NoPos)
  }
})
