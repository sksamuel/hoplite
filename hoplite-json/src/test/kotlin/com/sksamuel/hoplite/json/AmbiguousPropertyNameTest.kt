package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AmbiguousPropertyNameTest : DescribeSpec({

  data class Ambiguous(val someCamelSetting: String, val somecamelsetting: String, val SOMECAMELSETTING: String)

  describe("loading property differing in case from envs") {
    it("with path normalizer can disambiguate") {
      run {
        ConfigLoaderBuilder.defaultWithoutPropertySources()
          .withReport()
          .addPropertySource(
            EnvironmentVariablesPropertySource(
              environmentVariableMap = {
                mapOf(
                  "SOMECAMELSETTING" to "c",
                  "somecamelsetting" to "b",
                  "someCamelSetting" to "a",
                )
              }
            )
          )
          .build()
          .loadConfigOrThrow<Ambiguous>()
      } shouldBe Ambiguous("a", "b", "c")
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
              environmentVariableMap = {
                mapOf(
                  "SOMECAMELSETTING" to "c",
                  "somecamelsetting" to "b",
                  "someCamelSetting" to "a",
                )
              }
            )
          )
        .build()
        .loadConfigOrThrow<Ambiguous>()
      } shouldBe Ambiguous("a", "b", "c")
    }
  }
})
