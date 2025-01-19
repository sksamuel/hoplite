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
      environmentVariableMap = { mapOf("A" to "a", "A_B" to "ab", "A_B_CD" to "abcd") },
    ).node(PropertySourceContext.empty).getUnsafe()

    PathNormalizer.transform(node, null) shouldBe MapNode(
      map = mapOf(
        "a" to MapNode(
          map = mapOf(
            "b" to MapNode(
              map = mapOf(
                "cd" to StringNode("abcd", Pos.env, DotPath("a", "b", "cd"), sourceKey = "A_B_CD", delimiter = "_"),
              ),
              Pos.env,
              DotPath("a", "b"),
              value = StringNode("ab", Pos.env, DotPath("a", "b"), sourceKey = "A_B", delimiter = "_"),
              sourceKey = "A_B",
              delimiter = "_",
            ),
          ),
          Pos.env,
          DotPath("a"),
          value = StringNode("a", Pos.env, DotPath("a"), sourceKey = "A", delimiter = "_"),
          sourceKey = "A",
          delimiter = "_",
        ),
      ),
      Pos.env,
      DotPath.root,
      delimiter = "_",
    )
  }
})
