package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.transformer.PathNormalizer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AmbiguousPropertyNameTest : DescribeSpec({

  data class Ambiguous(val someCamelSetting: String, val somecamelsetting: String)

  describe("loading property differing in case from envs") {
    it("with path normalizer cannot disambiguate") {
      run {
        ConfigLoader {
          withReport()
          addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = true,
              useSingleUnderscoresAsSeparator = false,
              allowUppercaseNames = false,
              environmentVariableMap = {
                mapOf(
                  "someCamelSetting" to "a",
                  "somecamelsetting" to "b",
                  "SOMECAMELSETTING" to "c",
                )
              }
            )
          )
        }.loadConfigOrThrow<Ambiguous>()
      } shouldBe Ambiguous("c", "c")
    }

    it("without path normalizer") {
      run {
        ConfigLoaderBuilder.empty()
          .addDefaultDecoders()
          .addDefaultResolvers()
          .addDefaultParamMappers()
          .addDefaultPropertySources()
          .addDefaultParsers()
          .withReport()
          .addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = true,
              useSingleUnderscoresAsSeparator = false,
              allowUppercaseNames = false,
              environmentVariableMap = {
                mapOf(
                  "someCamelSetting" to "a",
                  "somecamelsetting" to "b",
                  "SOMECAMELSETTING" to "c",
                )
              }
            )
          )
        .build()
        .loadConfigOrThrow<Ambiguous>()
      } shouldBe Ambiguous("a", "b")
    }
  }
})
