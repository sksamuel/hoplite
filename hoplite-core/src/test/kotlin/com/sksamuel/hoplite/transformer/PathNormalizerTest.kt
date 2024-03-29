package com.sksamuel.hoplite.transformer

import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PathNormalizerTest : FunSpec({
  test("normalizes paths") {
    val node = EnvironmentVariablesPropertySource(
      useUnderscoresAsSeparator = false,
      allowUppercaseNames = false,
      useSingleUnderscoresAsSeparator = false,
      environmentVariableMap = { mapOf("A" to "a", "A.B" to "ab", "A.B.CD" to "abcd") },
    ).node(PropertySourceContext.empty).getUnsafe()

    PathNormalizer.transform(node, null) shouldBe MapNode(
      map = mapOf(
        "a" to MapNode(
          map = mapOf(
            "b" to MapNode(
              map = mapOf(
                "cd" to StringNode("abcd", Pos.env, DotPath("a", "b", "cd"), sourceKey = "A.B.CD"),
              ),
              Pos.env,
              DotPath("a", "b"),
              value = StringNode("ab", Pos.env, DotPath("a", "b"), sourceKey = "A.B"),
              sourceKey = "A.B"
            ),
          ),
          Pos.env,
          DotPath("a"),
          value = StringNode("a", Pos.env, DotPath("a"), sourceKey = "A"),
          sourceKey = "A"
        ),
      ),
      Pos.env,
      DotPath.root,
    )
  }
})
