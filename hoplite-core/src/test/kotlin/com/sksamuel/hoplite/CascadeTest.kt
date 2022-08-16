package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
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

  test("cascade function should return all overrides") {

    val node1 = MapNode(
      mapOf(
        "a" to StringNode("foo", Pos.NoPos, DotPath("a")),
        "b" to MapNode(
          mapOf(
            "j" to StringNode("jen", Pos.NoPos, DotPath("b", "j")),
            "k" to StringNode("ken", Pos.SourcePos("y"), DotPath("b", "k"))
          ), Pos.NoPos,
          DotPath("b")
        ),
        "c" to StringNode("baz", Pos.NoPos, DotPath("c")),
      ),
      Pos.NoPos,
      DotPath.root,
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.SourcePos("x"), DotPath("b", "k")),
            "m" to StringNode("moz", Pos.NoPos, DotPath("b", "m")),
          ),
          Pos.NoPos,
          DotPath("b")
        ),
        "c" to StringNode("maz", Pos.NoPos, DotPath("c")),
      ),
      Pos.NoPos,
      DotPath.root,
    )

    node1.cascade(node2, CascadeMode.Merge).overrides shouldBe listOf(
      OverridePath(DotPath("b", "k"), Pos.SourcePos("y"), Pos.SourcePos("x")),
      OverridePath(DotPath("c"), Pos.NoPos, Pos.NoPos),
    )
  }

  test("CascadeMode.error should error if overrides present") {
    shouldThrowAny {
      ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          database.name = my database
          database.host = localhost
          database.port = 3306
          database.timeout = 100.0
          database.tls = true
          """.trimIndent(), "props"
          )
        )
        .addPropertySource(
          PropertySource.string(
            """
          database.port = 1234
          database.tls = false
          """.trimIndent(), "props"
          )
        )
        .withCascadeMode(CascadeMode.Error)
        .build()
        .loadNodeOrThrow()
    }.message shouldBe "Error loading config because:\n" +
      "\n" +
      "    Overridden configs are configured as errors\n" +
      "     - database.port at (props string source) overriden by (props string source)\n" +
      "     - database.tls at (props string source) overriden by (props string source)"
  }
})
