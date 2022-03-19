package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvironmentVariablesPropertySourceTest : FunSpec({

  test("build env source should include paths") {
    EnvironmentVariablesPropertySource(
      useUnderscoresAsSeparator = false,
      allowUppercaseNames = false
    ) { mapOf("a" to "foo", "a.b" to "bar", "c" to "baz") }.node(
      PropertySourceContext.empty
    ).getUnsafe() shouldBe MapNode(
      mapOf(
        "a" to MapNode(
          value = StringNode("foo", Pos.env, DotPath("a")),
          map = mapOf("b" to StringNode("bar", Pos.env, DotPath("a", "b"))),
          pos = Pos.SourcePos("env"),
          path = DotPath("a"),
        ),
        "c" to StringNode("baz", Pos.env, DotPath("c")),
      ),
      pos = Pos.env,
      DotPath.root,
    )
  }

})
