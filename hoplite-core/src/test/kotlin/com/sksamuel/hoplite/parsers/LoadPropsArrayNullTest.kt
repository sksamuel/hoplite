package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.sources.MapPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class LoadPropsArrayNullTest : FunSpec({

  // A null element in a list value must be preserved at its position. Previously the transform
  // used mapNotNull, which dropped nulls and shifted every later element left by one — so
  // ["x", null, "y"] decoded to index 1 = "y" instead of null.
  test("a null element in a list value is preserved at its index") {
    data class Cfg(val a: List<String?>)

    val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .addPropertySource(MapPropertySource(mapOf("a" to listOf("x", null, "y"))))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.a shouldBe listOf("x", null, "y")
  }
})
