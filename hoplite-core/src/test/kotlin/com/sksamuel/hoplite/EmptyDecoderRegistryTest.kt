package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EmptyDecoderRegistryTest : FunSpec() {
  init {
    test("empty decoder registry throws error") {
      data class Config(val a: String)
      val parsers = defaultParserRegistry()
      val sources = defaultPropertySources()
      val preprocessors = defaultPreprocessors()
      val mappers = defaultParamMappers()
      val e = ConfigLoader(
        DecoderRegistry.zero,
        sources,
        parsers,
        preprocessors,
        mappers,
        allowEmptyTree = false,
        allowUnresolvedSubstitutions = false,
      ).loadConfig<Config>()
      e as Validated.Invalid<ConfigFailure>
      e.error shouldBe ConfigFailure.EmptyDecoderRegistry
    }
  }
}
