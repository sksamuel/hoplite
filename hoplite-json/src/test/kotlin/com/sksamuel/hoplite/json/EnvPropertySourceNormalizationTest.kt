package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.transformer.PathNormalizer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EnvPropertySourceNormalizationTest : DescribeSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  describe("loading from envs") {
    it("with path normalizer") {
      run {
        ConfigLoader {
          addNodeTransformer(PathNormalizer)
          addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = true,
              useSingleUnderscoresAsSeparator = false,
              allowUppercaseNames = false,
              environmentVariableMap = {
                mapOf(
                  "CREDS__USERNAME" to "a",
                  "CREDS__PASSWORD" to "c",
                  "SOMECAMELSETTING" to "c"
                )
              }
            )
          )
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "c"), "c")
    }

    it("with path normalizer and kebab case underscore separator") {
      run {
        ConfigLoader {
          addNodeTransformer(PathNormalizer)
          addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = true,
              useSingleUnderscoresAsSeparator = false,
              allowUppercaseNames = false,
              environmentVariableMap = {
                mapOf(
                  "CREDS__USERNAME" to "a",
                  "CREDS__PASSWORD" to "c",
                  "SOME_CAMEL_SETTING" to "c"
                )
              }
            )
          )
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "c"), "c")
    }

    it("with path normalizer and underscore separator") {
      run {
        ConfigLoader {
          addNodeTransformer(PathNormalizer)
          addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = false,
              useSingleUnderscoresAsSeparator = true,
              allowUppercaseNames = true,
              environmentVariableMap = {
                mapOf(
                  "CREDS_USERNAME" to "a",
                  "CREDS_PASSWORD" to "b",
                  "SOMECAMELSETTING" to "c"
                )
              }
            )
          )
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "b"), "c")
    }

    it("with path normalizer and lowercase names") {
      run {
        ConfigLoader {
          addNodeTransformer(PathNormalizer)
          addPropertySource(EnvironmentVariablesPropertySource(
            useUnderscoresAsSeparator = false,
            useSingleUnderscoresAsSeparator = true,
            allowUppercaseNames = false,
            environmentVariableMap = {
              mapOf(
                "creds_username" to "a",
                "creds_password" to "d",
                "somecamelsetting" to "e"
              )
            }
          ))
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "d"), "e")
    }

    it("with path normalizer and prefix") {
      run {
        ConfigLoader {
          addNodeTransformer(PathNormalizer)
          addPropertySource(
            EnvironmentVariablesPropertySource(
              useUnderscoresAsSeparator = false,
              useSingleUnderscoresAsSeparator = true,
              allowUppercaseNames = false,
              environmentVariableMap = {
                mapOf(
                  "WIBBLE_CREDS_USERNAME" to "a",
                  "WIBBLE_CREDS_PASSWORD" to "c",
                  "WIBBLE_SOMECAMELSETTING" to "c"
                )
              },
              prefix = "WIBBLE_"
            )
          )
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "c"), "c")
    }
  }

})
