package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.MapPropertySource
import com.sksamuel.hoplite.transformer.PathNormalizer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class CascadingNormalizationTest : FunSpec() {
  init {
    test("Parameter normalization works with cascading") {
      data class SubSection(val someValue: Int)
      data class Section(val test: Int, val subSection: SubSection)
      data class TestConfig(val section: Section)

      val configInputs = mapOf("section" to mapOf("test" to 1, "sub-section" to mapOf("some-value" to 2)))

      val config = ConfigLoaderBuilder.newBuilder()
        .addNodeTransformer(PathNormalizer)
        .addPropertySource(
          EnvironmentVariablesPropertySource(
            useUnderscoresAsSeparator = false,
            allowUppercaseNames = false,
            useSingleUnderscoresAsSeparator = false,
            environmentVariableMap = { mapOf("section.subSection.someValue" to "3") }
          )
        )
        .addPropertySource(MapPropertySource(configInputs))
        .build()
        .loadConfigOrThrow<TestConfig>()

      config.section.subSection.someValue shouldBe 3
    }
  }

}
