package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.internal.Cascader
import com.sksamuel.hoplite.internal.OverridePath
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class CascadeTest : FunSpec({

  test("CascadeMode.Merge should work with two maps at immediate depth") {

    val cascader = Cascader(CascadeMode.Merge, false)

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

    val f = cascader.cascade(node1, node2).node
    f["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    f["b"] shouldBe StringNode("bar", Pos.NoPos, DotPath.root)
    f["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)

    val g = cascader.cascade(node2, node1).node
    g["a"] shouldBe StringNode("faz", Pos.NoPos, DotPath.root)
    g["b"] shouldBe StringNode("bar", Pos.NoPos, DotPath.root)
    g["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)
  }

  test("CascadeMode.Merge should work with two maps at arbitrary depth") {

    val cascader = Cascader(CascadeMode.Merge, false)

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
      DotPath.root
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
      DotPath.root
    )

    val f = cascader.cascade(node1, node2).node
    f["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    f["b"]["j"] shouldBe StringNode("jen", Pos.NoPos, DotPath.root)
    f["b"]["k"] shouldBe StringNode("ken", Pos.NoPos, DotPath.root)
    f["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)

    val g = cascader.cascade(node2, node1).node
    g["a"] shouldBe StringNode("foo", Pos.NoPos, DotPath.root)
    g["b"]["j"] shouldBe StringNode("jen", Pos.NoPos, DotPath.root)
    g["b"]["k"] shouldBe StringNode("kez", Pos.NoPos, DotPath.root)
    g["c"] shouldBe StringNode("baz", Pos.NoPos, DotPath.root)
  }

  test("CascadeMode.Fallthrough should take an entire map if present") {

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
      DotPath.root
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.NoPos, DotPath("b", "k")),
            "m" to StringNode("moz", Pos.NoPos, DotPath("b", "m"))
          ),
          Pos.NoPos,
          DotPath("b")
        ),
        "c" to StringNode("baz", Pos.NoPos, DotPath("c"))
      ),
      Pos.NoPos,
      DotPath.root
    )

    val cascader = Cascader(CascadeMode.Fallthrough, false)
    val merged = cascader.cascade(node1, node2).node
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
        "c" to StringNode("baz", Pos.NoPos, DotPath("c"))
      ),
      Pos.NoPos,
      DotPath.root
    )

    val node2 = MapNode(
      mapOf(
        "b" to MapNode(
          mapOf(
            "k" to StringNode("kez", Pos.SourcePos("x"), DotPath("b", "k")),
            "m" to StringNode("moz", Pos.NoPos, DotPath("b", "m"))
          ),
          Pos.NoPos,
          DotPath("b")
        ),
        "c" to StringNode("maz", Pos.NoPos, DotPath("c"))
      ),
      Pos.NoPos,
      DotPath.root
    )

    val cascader = Cascader(CascadeMode.Merge, false)
    cascader.cascade(node1, node2).overrides shouldBe listOf(
      OverridePath(DotPath("b", "k"), Pos.SourcePos("y"), Pos.SourcePos("x")),
      OverridePath(DotPath("c"), Pos.NoPos, Pos.NoPos)
    )
  }

  // gh-592: a higher-precedence map sharing a key with a lower-precedence scalar must not
  // discard the scalar. The merged node keeps the map's children AND carries the scalar
  // through as its `value`, so the key still decodes as a scalar.
  test("CascadeMode.Merge must preserve a scalar when a higher-precedence map shares the key") {
    val map = MapNode(
      mapOf("foo" to StringNode("bar", Pos.NoPos, DotPath("host", "foo"))),
      Pos.NoPos,
      DotPath("host"),
    )
    val scalar = StringNode("0.0.0.0", Pos.NoPos, DotPath("host"))

    // a (the map) is the higher-precedence node; b (the scalar) must not be dropped.
    val merged = Cascader(CascadeMode.Merge, false).cascade(map, scalar).node as MapNode
    merged.value shouldBe scalar
    merged["foo"] shouldBe StringNode("bar", Pos.NoPos, DotPath("host", "foo"))
  }

  test("cascade function should report an override on a map node's own value") {
    // A "dual" node is both a leaf value and a map (e.g. props `db = primary` plus `db.host = ...`),
    // represented as a MapNode with a non-Undefined `value`. When two sources both define such a
    // value, the conflict on the value itself must be reported as an override.
    val node1 = MapNode(
      map = mapOf("host" to StringNode("localhost", Pos.NoPos, DotPath("db", "host"))),
      pos = Pos.NoPos,
      path = DotPath("db"),
      value = StringNode("primary", Pos.SourcePos("y"), DotPath("db"))
    )

    val node2 = MapNode(
      map = emptyMap(),
      pos = Pos.NoPos,
      path = DotPath("db"),
      value = StringNode("secondary", Pos.SourcePos("x"), DotPath("db"))
    )

    val cascader = Cascader(CascadeMode.Merge, false)
    cascader.cascade(node1, node2).overrides shouldBe listOf(
      OverridePath(DotPath("db"), Pos.SourcePos("y"), Pos.SourcePos("x"))
    )
  }

  test("a map's own value without a discriminator field still merges in explicit sealed types mode") {
    // props `db = primary` plus `db.host = ...` is a "dual" node, not a sealed subtype selection,
    // so a lower-precedence source's children must not be dropped because its value differs.
    val node1 = MapNode(
      map = mapOf("host" to StringNode("localhost", Pos.NoPos, DotPath("db", "host"))),
      pos = Pos.NoPos,
      path = DotPath("db"),
      value = StringNode("primary", Pos.NoPos, DotPath("db"))
    )

    val node2 = MapNode(
      map = mapOf("port" to StringNode("3306", Pos.NoPos, DotPath("db", "port"))),
      pos = Pos.NoPos,
      path = DotPath("db"),
      value = StringNode("secondary", Pos.NoPos, DotPath("db"))
    )

    val merged = Cascader(CascadeMode.Merge, false, sealedTypeDiscriminatorField = "_type")
      .cascade(node1, node2).node as MapNode
    merged.value shouldBe StringNode("primary", Pos.NoPos, DotPath("db"))
    merged.map.keys shouldBe setOf("host", "port")
  }

  test("a map selecting a sealed subtype overrides only the lower-precedence map's selection") {
    val node1 = MapNode(
      map = mapOf("x" to StringNode("abc", Pos.SourcePos("env"), DotPath("db", "x"))),
      pos = Pos.SourcePos("env"),
      path = DotPath("db"),
      value = StringNode("Foo", Pos.SourcePos("env"), DotPath("db"))
    )

    val node2 = MapNode(
      map = mapOf(
        "_type" to StringNode("Bar", Pos.LinePos(2, "file"), DotPath("db", "_type")),
        "y" to StringNode("xyz", Pos.LinePos(3, "file"), DotPath("db", "y")),
      ),
      pos = Pos.LinePos(1, "file"),
      path = DotPath("db"),
    )

    val result = Cascader(CascadeMode.Merge, false, sealedTypeDiscriminatorField = "_type").cascade(node1, node2)
    val merged = result.node as MapNode
    merged.value shouldBe StringNode("Foo", Pos.SourcePos("env"), DotPath("db"))
    merged.map.keys shouldBe setOf("x", "y")
    // the override points at the selections, i.e. the lower-precedence map's discriminator field, not the map
    result.overrides shouldBe listOf(OverridePath(DotPath("db"), Pos.SourcePos("env"), Pos.LinePos(2, "file")))
  }

  test("a scalar over a map folds into the map, replacing its selection, in explicit sealed types mode") {
    val scalar = StringNode("Foo", Pos.SourcePos("env"), DotPath("db"))
    val map = MapNode(
      mapOf(
        "_type" to StringNode("Bar", Pos.SourcePos("file"), DotPath("db", "_type")),
        "x" to StringNode("abc", Pos.SourcePos("file"), DotPath("db", "x")),
      ),
      Pos.SourcePos("file"),
      DotPath("db"),
    )

    val merged = Cascader(CascadeMode.Merge, false, sealedTypeDiscriminatorField = "_type")
      .cascade(scalar, map).node as MapNode
    merged.value shouldBe scalar
    merged.map.keys shouldBe setOf("x")
  }

  test("a map selecting a sealed subtype overrides a lower-precedence scalar selecting one") {
    val map = MapNode(
      map = mapOf(
        "_type" to StringNode("Foo", Pos.LinePos(2, "file"), DotPath("db", "_type")),
        "x" to StringNode("abc", Pos.LinePos(3, "file"), DotPath("db", "x")),
      ),
      pos = Pos.LinePos(1, "file"),
      path = DotPath("db"),
    )
    val scalar = StringNode("Bar", Pos.SourcePos("env"), DotPath("db"))

    val result = Cascader(CascadeMode.Merge, false, sealedTypeDiscriminatorField = "_type").cascade(map, scalar)
    result.node shouldBe map
    result.overrides shouldBe listOf(OverridePath(DotPath("db"), Pos.LinePos(2, "file"), Pos.SourcePos("env")))
  }

  test("a lower-precedence scalar selects the sealed subtype of a map that doesn't select one") {
    val map = MapNode(
      map = mapOf("x" to StringNode("abc", Pos.SourcePos("env"), DotPath("db", "x"))),
      pos = Pos.SourcePos("env"),
      path = DotPath("db"),
    )
    val scalar = StringNode("Foo", Pos.LinePos(1, "file"), DotPath("db"))

    val result = Cascader(CascadeMode.Merge, false, sealedTypeDiscriminatorField = "_type").cascade(map, scalar)
    val merged = result.node as MapNode
    merged.value shouldBe scalar
    merged.map.keys shouldBe setOf("x")
    result.overrides shouldBe emptyList()
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
      "     - database.port at (props string source) overridden by (props string source)\n" +
      "     - database.tls at (props string source) overridden by (props string source)"
  }

  // Regression for the direction of OverrideConfigError's English: with two sources at
  // *distinguishable* positions, the message must read "X at <loser> overridden by <winner>".
  // The earlier wording rendered as "X at <winner> overridden by <loser>" — backwards from
  // who actually beat whom in the cascade.
  test("CascadeMode.Error message names the loser before the winner") {
    val winner = java.util.Properties().apply { setProperty("database.port", "3306") }
    val loser = java.util.Properties().apply { setProperty("database.port", "1234") }

    val ex = shouldThrowAny {
      ConfigLoaderBuilder.default()
        // first added = highest priority = cascade winner
        .addPropertySource(com.sksamuel.hoplite.parsers.PropsPropertySource(winner, name = "winner-source"))
        .addPropertySource(com.sksamuel.hoplite.parsers.PropsPropertySource(loser, name = "loser-source"))
        .withCascadeMode(CascadeMode.Error)
        .build()
        .loadNodeOrThrow()
    }
    // <loser> rendered first, <winner> after "overridden by"
    ex.message shouldBe "Error loading config because:\n" +
      "\n" +
      "    Overridden configs are configured as errors\n" +
      "     - database.port at (loser-source) overridden by (winner-source)"
  }
})
