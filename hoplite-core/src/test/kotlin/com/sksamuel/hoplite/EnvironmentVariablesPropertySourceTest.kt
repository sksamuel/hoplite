package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvironmentVariablesPropertySourceTest : FunSpec({

  test("build env source with the classic env var mapping should include paths") {
    EnvironmentVariablesPropertySource(
      useUnderscoresAsSeparator = true,
      useSingleUnderscoresAsSeparator = false,
      allowUppercaseNames = false,
      environmentVariableMap = { mapOf("a" to "foo", "a.b" to "bar", "c" to "baz", "d__e" to "gaz") }
    ).node(
      PropertySourceContext.empty
    ).getUnsafe() shouldBe MapNode(
      mapOf(
        "a" to MapNode(
          value = StringNode("foo", Pos.env, DotPath("a"), sourceKey = "a"),
          map = mapOf("b" to StringNode("bar", Pos.env, DotPath("a", "b"), sourceKey = "a.b")),
          pos = Pos.SourcePos("env"),
          path = DotPath("a"),
          sourceKey = "a"
        ),
        "c" to StringNode("baz", Pos.env, DotPath("c"), sourceKey = "c"),
        "d" to MapNode(
          value = Undefined,
          map = mapOf("e" to StringNode("gaz", Pos.env, DotPath("d", "e"), sourceKey = "d__e")),
          pos = Pos.SourcePos("env"),
          path = DotPath("d"),
          sourceKey = null
        ),
      ),
      pos = Pos.env,
      DotPath.root
    )
  }

  test("build env source with the idiomatic env var mapping should include paths") {
    EnvironmentVariablesPropertySource(
      useUnderscoresAsSeparator = false,
      useSingleUnderscoresAsSeparator = true,
      allowUppercaseNames = false,
      environmentVariableMap = { mapOf("a" to "foo", "a.b" to "bar", "c" to "baz", "d_e" to "gaz") }
    ).node(
      PropertySourceContext.empty
    ).getUnsafe() shouldBe MapNode(
      mapOf(
        "a" to MapNode(
          value = StringNode("foo", Pos.env, DotPath("a"), sourceKey = "a"),
          map = mapOf("b" to StringNode("bar", Pos.env, DotPath("a", "b"), sourceKey = "a.b")),
          pos = Pos.SourcePos("env"),
          path = DotPath("a"),
          sourceKey = "a"
        ),
        "c" to StringNode("baz", Pos.env, DotPath("c"), sourceKey = "c"),
        "d" to MapNode(
          value = Undefined,
          map = mapOf("e" to StringNode("gaz", Pos.env, DotPath("d", "e"), sourceKey = "d_e")),
          pos = Pos.SourcePos("env"),
          path = DotPath("d"),
          sourceKey = null
        ),
      ),
      pos = Pos.env,
      DotPath.root
    )
  }

})
