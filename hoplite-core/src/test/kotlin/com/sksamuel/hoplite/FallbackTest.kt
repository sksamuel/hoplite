package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FallbackTest : FunSpec({

  test("fallback should work with two maps at immediate depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.None),
        "b" to StringNode("bar", Pos.None)
      ),
      Pos.None
    )

    val node2 = MapNode(
      mapOf(
        "a" to StringNode("faz", Pos.None),
        "c" to StringNode("baz", Pos.None)
      ),
      Pos.None
    )

    val f = node1.merge(node2)
    f["a"] shouldBe StringNode("foo", Pos.None)
    f["b"] shouldBe StringNode("bar", Pos.None)
    f["c"] shouldBe StringNode("baz", Pos.None)

    val g = node2.merge(node1)
    g["a"] shouldBe StringNode("faz", Pos.None)
    g["b"] shouldBe StringNode("bar", Pos.None)
    g["c"] shouldBe StringNode("baz", Pos.None)
  }

  test("fallback should work with two maps at arbitrary depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.None),
        "b" to MapNode(
          mapOf(
            "j" to StringNode("jen", Pos.None),
            "k" to StringNode("ken", Pos.None)
          ),
          Pos.None
        )
      ),
      Pos.None
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.None)
          ),
          Pos.None
        ),
        "c" to StringNode("baz", Pos.None)
      ),
      Pos.None
    )

    val f = node1.merge(node2)
    f["a"] shouldBe StringNode("foo", Pos.None)
    f["b"]["j"] shouldBe StringNode("jen", Pos.None)
    f["b"]["k"] shouldBe StringNode("ken", Pos.None)
    f["c"] shouldBe StringNode("baz", Pos.None)

    val g = node2.merge(node1)
    g["a"] shouldBe StringNode("foo", Pos.None)
    g["b"]["j"] shouldBe StringNode("jen", Pos.None)
    g["b"]["k"] shouldBe StringNode("kez", Pos.None)
    g["c"] shouldBe StringNode("baz", Pos.None)
  }
})
