package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CascadeTest : FunSpec({

  test("CascadeMode.Merge should work with two maps at immediate depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos, DotPath.root),
        "b" to StringNode("bar", Pos.NoPos, DotPath.root)
      ),
      Pos.NoPos, DotPath.root
    )

    val node2 = MapNode(
      mapOf(
        "a" to StringNode("faz", Pos.NoPos, DotPath.root),
        "c" to StringNode("baz", Pos.NoPos, DotPath.root)
      ),
      Pos.NoPos,
      DotPath.root
    )

    val f = node1.cascade(node2, CascadeMode.Merge).node
    f["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    f["b"] shouldBe StringNode("bar", Pos.NoPos, DotPath.root)
    f["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)

    val g = node2.cascade(node1, CascadeMode.Merge).node
    g["a"] shouldBe StringNode("faz", Pos.NoPos, DotPath.root)
    g["b"] shouldBe StringNode("bar", Pos.NoPos, DotPath.root)
    g["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)
  }

  test("CascadeMode.Merge should work with two maps at arbitrary depth") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos, DotPath.root),
        "b" to MapNode(
          mapOf(
            "j" to StringNode("jen", Pos.NoPos, DotPath.root),
            "k" to StringNode("ken", Pos.NoPos, DotPath.root)
          ), Pos.NoPos,
          DotPath.root
        )
      ),
      Pos.NoPos,
      DotPath.root,
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.NoPos, DotPath.root)
          ),
          Pos.NoPos,
          DotPath.root
        ),
        "c" to StringNode("baz", Pos.NoPos, DotPath.root)
      ),
      Pos.NoPos,
      DotPath.root,
    )

    val f = node1.cascade(node2, CascadeMode.Merge).node
    f["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    f["b"]["j"] shouldBe StringNode("jen", Pos.NoPos, DotPath.root)
    f["b"]["k"] shouldBe StringNode("ken", Pos.NoPos, DotPath.root)
    f["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)

    val g = node2.cascade(node1, CascadeMode.Merge).node
    g["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    g["b"]["j"] shouldBe StringNode("jen", Pos.NoPos, DotPath.root)
    g["b"]["k"] shouldBe StringNode("kez", Pos.NoPos, DotPath.root)
    g["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)
  }

  test("CascadeMode.Override should take an entire map if present") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos, DotPath("a")),
        "b" to MapNode(
          mapOf(
            "j" to StringNode("jen", Pos.NoPos, DotPath("b", "j")),
            "k" to StringNode("ken", Pos.NoPos, DotPath("b", "k"))
          ), Pos.NoPos,
          DotPath("b")
        )
      ),
      Pos.NoPos,
      DotPath.root,
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.NoPos, DotPath("b", "k")),
            "m" to StringNode("moz", Pos.NoPos, DotPath("b", "m")),
          ),
          Pos.NoPos,
          DotPath("b")
        ),
        "c" to StringNode("baz", Pos.NoPos, DotPath("c"))
      ),
      Pos.NoPos,
      DotPath.root,
    )

    val merged = node1.cascade(node2, CascadeMode.Override).node
    merged["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath("a"))
    merged["b"] shouldBe MapNode(
      mapOf(
        "j" to StringNode("jen", Pos.NoPos, DotPath("b", "j")),
        "k" to StringNode("ken", Pos.NoPos, DotPath("b", "k"))
      ), Pos.NoPos,
      DotPath("b")
    )
    merged["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath("c"))
  }
})
