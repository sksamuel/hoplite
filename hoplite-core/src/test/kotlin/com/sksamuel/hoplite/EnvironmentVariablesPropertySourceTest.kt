package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvironmentVariablesPropertySourceTest : FunSpec({

  test("build env source should include paths") {
    EnvironmentVariablesPropertySource(
      useUnderscoresAsSeparator = false,
      allowUppercaseNames = false,
      environmentVariableMap = { mapOf("a" to "foo", "a.b" to "bar", "c" to "baz") }
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
      ),
      pos = Pos.env,
      DotPath.root
    )
  }

})
