package com.sksamuel.hoplite.secrets

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.PropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalHoplite::class)
class FailOnWeakSecretsTest : FunSpec() {
  init {
    test("failOnWeakSecrets should exit if any secret is weak") {

      data class Config(
        val host: String,
        val pass: String,
      )

      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addPropertySource(
            PropertySource.string(
              """
          host = my database
          pass = fake_password
          """.trimIndent(), "props"
            )
          )
          .withReport()
          .failOnWeakSecrets()
          .withSecretStrengthAnalyzer(SecretStrengthAnalyzer.default)
          .build()
          .loadConfigOrThrow<Config>()
      }.message.shouldContain(
        """Error loading config because:

    Weak secret 'pass' - Does not contain a digit

    Weak secret 'host' - Too short"""
      )
    }

    test("failOnWeakSecrets should error if no secret analyzer is specified") {

      data class Config(
        val host: String,
        val pass: String,
      )

      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addPropertySource(
            PropertySource.string(
              """
          host = my database
          pass = fake_password
          """.trimIndent(), "props"
            )
          )
          .withReport()
          .failOnWeakSecrets()
          .build()
          .loadConfigOrThrow<Config>()
      }.message.shouldContain(
        """failOnWeakSecrets is enabled but no secret-strength-analyzer is specified"""
      )
    }

  }
}
