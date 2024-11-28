package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvironmentVariablesPropertySourceTest : FunSpec({

  test("build env source with the classic env var mapping should include paths") {
    EnvironmentVariablesPropertySource(
      environmentVariableMap = { mapOf("a" to "foo", "a_b" to "bar", "c" to "baz", "d_e" to "gaz") }
    ).node(
      PropertySourceContext.empty
    ).getUnsafe() shouldBe MapNode(
      mapOf(
        "a" to MapNode(
          value = StringNode("foo", Pos.env, DotPath("a"), sourceKey = "a", delimiter = "_"),
          map = mapOf("b" to StringNode("bar", Pos.env, DotPath("a", "b"), sourceKey = "a_b", delimiter = "_")),
          pos = Pos.SourcePos("env"),
          path = DotPath("a"),
          delimiter = "_",
          sourceKey = "a"
        ),
        "c" to StringNode("baz", Pos.env, DotPath("c"), sourceKey = "c", delimiter = "_"),
        "d" to MapNode(
          value = Undefined,
          map = mapOf("e" to StringNode("gaz", Pos.env, DotPath("d", "e"), sourceKey = "d_e", delimiter = "_")),
          pos = Pos.SourcePos("env"),
          path = DotPath("d"),
          delimiter = "_",
          sourceKey = "d"
        ),
      ),
      pos = Pos.env,
      path = DotPath.root,
      delimiter = "_",
    )
  }

  test("build env source with the idiomatic env var mapping should include paths") {
    EnvironmentVariablesPropertySource(
      environmentVariableMap = { mapOf("a" to "foo", "a_b" to "bar", "c" to "baz", "d_e" to "gaz") }
    ).node(
      PropertySourceContext.empty
    ).getUnsafe() shouldBe MapNode(
      mapOf(
        "a" to MapNode(
          value = StringNode("foo", Pos.env, DotPath("a"), sourceKey = "a", delimiter = "_"),
          map = mapOf("b" to StringNode("bar", Pos.env, DotPath("a", "b"), sourceKey = "a_b", delimiter = "_")),
          pos = Pos.SourcePos("env"),
          path = DotPath("a"),
          delimiter = "_",
          sourceKey = "a"
        ),
        "c" to StringNode("baz", Pos.env, DotPath("c"), sourceKey = "c", delimiter = "_"),
        "d" to MapNode(
          value = Undefined,
          map = mapOf("e" to StringNode("gaz", Pos.env, DotPath("d", "e"), sourceKey = "d_e", delimiter = "_")),
          pos = Pos.SourcePos("env"),
          path = DotPath("d"),
          delimiter = "_",
          sourceKey = "d"
        ),
      ),
      pos = Pos.env,
      path = DotPath.root,
      delimiter = "_",
    )
  }

  test("build env source can create case sensitive Maps") {
    data class TestConfig(val mapProp: Map<String, String>)

    val config = ConfigLoaderBuilder
      .defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(
          environmentVariableMap = {
            mapOf(
              "MAPPROP_a" to "value_a",
              "MAPPROP_A" to "value_A",
              "MAPPROP_abc" to "value_abc",
              "MAPPROP_ABC" to "value_ABC",
            )
          },
        )
      )
      .build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(mapOf(
      "a" to "value_a",
      "A" to "value_A",
      "abc" to "value_abc",
      "ABC" to "value_ABC",
    ))
  }

  test("env var source should respect config aliases that need to be normalized to match") {
    data class TestConfig(@ConfigAlias("fooBar") val bazBar: String)

    val config = ConfigLoaderBuilder
      .defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(
          environmentVariableMap = {
            mapOf("FOOBAR" to "fooValue")
          },
        )
      )
      .build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig("fooValue")
  }

})
