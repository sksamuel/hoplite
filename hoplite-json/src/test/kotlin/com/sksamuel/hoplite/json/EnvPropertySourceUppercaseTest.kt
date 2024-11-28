package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EnvPropertySourceUppercaseTest : DescribeSpec({

  data class Creds(val username: String, val password: String)
  data class Config(val creds: Creds, val someCamelSetting: String)

  describe("loading from envs") {
    it("with default options") {
      run {
        ConfigLoader {
          addPropertySource(
            EnvironmentVariablesPropertySource(
              environmentVariableMap = {
                mapOf(
                  "CREDS_USERNAME" to "a",
                  "CREDS_PASSWORD" to "c",
                  "SOMECAMELSETTING" to "c"
                )
              }
            )
          )
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "c"), "c")
    }

    it("with underscore separator") {
      run {
        ConfigLoader {
          addPropertySource(
            EnvironmentVariablesPropertySource(
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

    it("with lowercase names") {
      run {
        ConfigLoader {
          addPropertySource(EnvironmentVariablesPropertySource(
            environmentVariableMap = {
              mapOf(
                "creds_username" to "a",
                "creds_password" to "d",
                "someCamelSetting" to "e"
              )
            }
          ))
        }.loadConfigOrThrow<Config>()
      } shouldBe Config(Creds("a", "d"), "e")
    }

    it("with prefix") {
      run {
        ConfigLoader {
          addPropertySource(
            EnvironmentVariablesPropertySource(
              environmentVariableMap = {
                mapOf(
                  "OTHER_CREDS_USERNAME" to "z",
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
