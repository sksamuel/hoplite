package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvVarListOrderingTest : FunSpec({

  // EnvironmentVariablesPropertySource builds a MapNode keyed on the integer index strings, then
  // converts to an ArrayNode by taking `node.map.values.toList()`. The intermediate map is a
  // HashMap (via hashMapOf()), so for a long-enough list the values come out in HashMap iteration
  // order — which is not the integer-index order. The list ends up correctly sized but with the
  // elements shuffled.
  test("list elements should appear in integer-index order regardless of env iteration order") {
    data class Cfg(val items: List<String>)

    // 16 items is enough to push HashMap into a non-trivial bucket layout for Java 17+'s default
    // HashMap (initial capacity 16). The reverse-insertion order makes the ordering bug surface
    // even with implementations that happen to be insertion-order-stable for small maps.
    val env = (0..15).reversed().associate { "ITEMS_$it" to "v$it" }

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.items shouldBe (0..15).map { "v$it" }
  }
})
