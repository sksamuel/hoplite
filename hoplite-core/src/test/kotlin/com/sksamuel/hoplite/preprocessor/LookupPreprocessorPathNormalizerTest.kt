package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LookupPreprocessorPathNormalizerTest : FunSpec({

  // Regression for #503: PathNormalizer lowercases (and strips -/_ from) all map keys, but the
  // LookupPreprocessor looked up ${...} paths against the normalized tree using the raw, still-cased
  // key — so a lookup of a mixed-case key could no longer find its (now lowercased) value.
  test("lookup of a mixed-case key resolves when PathNormalizer is active") {
    data class Cfg(val fooBar: String, val result: String)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("fooBar" to "hello", "result" to "\${fooBar}"))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.result shouldBe "hello"
  }
})
