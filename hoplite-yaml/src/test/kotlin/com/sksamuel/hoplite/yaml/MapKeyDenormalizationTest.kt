package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapKeyDenormalizationTest : FunSpec({

  data class Worker(val batchSize: Int)
  data class Application(val workers: Map<String, Worker>)
  data class Config(val application: Application)

  data class Logger(val level: String)
  data class LoggersConfig(val loggers: Map<String, Logger>)

  // gh-603: overriding one YAML map entry with an env var changed the keys of the untouched
  // siblings to their full dotted path
  test("an env var override of one yaml map entry does not change the keys of untouched siblings") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(environmentVariableMap = {
          mapOf("APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234")
        })
      )
      .addPropertySource(
        PropertySource.string(
          """
          application:
            workers:
              alphaWorker:
                batchSize: 10
              betaWorker:
                batchSize: 20
          """.trimIndent(),
          "yaml",
        )
      )
      .build()
      .loadConfigOrThrow<Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Worker(1234),
      "betaWorker" to Worker(20),
    )
  }

  test("yaml map keys containing dots are preserved when an env var adds an entry to the same map") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("LOGGERS_root_LEVEL" to "warn") })
      )
      .addPropertySource(
        PropertySource.string(
          """
          loggers:
            com.example:
              level: debug
            org.other-thing:
              level: info
          """.trimIndent(),
          "yaml",
        )
      )
      .build()
      .loadConfigOrThrow<LoggersConfig>()

    config.loggers shouldBe mapOf(
      "root" to Logger("warn"),
      "com.example" to Logger("debug"),
      "org.other-thing" to Logger("info"),
    )
  }

  test("map keys are preserved when an env var overrides a map nested under a key containing dots") {
    data class DottedConfig(val apps: Map<String, Map<String, Worker>>)

    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("APPS_ROOT_alphaWorker_BATCHSIZE" to "1234") })
      )
      .addPropertySource(
        PropertySource.string(
          """
          apps:
            com.example:
              alphaWorker:
                batchSize: 10
              betaWorker:
                batchSize: 20
          """.trimIndent(),
          "yaml",
        )
      )
      .build()
      .loadConfigOrThrow<DottedConfig>()

    config.apps shouldBe mapOf(
      "ROOT" to mapOf("alphaWorker" to Worker(1234)),
      "com.example" to mapOf("alphaWorker" to Worker(10), "betaWorker" to Worker(20)),
    )
  }

  test("an aliased map entry uses the alias's key, not the anchor's") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        PropertySource.string(
          """
          base: &base
            batchSize: 5
          application:
            workers:
              alphaWorker: *base
              betaWorker:
                batchSize: 20
          """.trimIndent(),
          "yaml",
        )
      )
      .build()
      .loadConfigOrThrow<Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Worker(5),
      "betaWorker" to Worker(20),
    )
  }

  test("an aliased map uses its own keys when an env var overrides one of its entries") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        EnvironmentVariablesPropertySource(environmentVariableMap = {
          mapOf("APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234")
        })
      )
      .addPropertySource(
        PropertySource.string(
          """
          defaults: &defaults
            alphaWorker:
              batchSize: 10
            betaWorker:
              batchSize: 20
          application:
            workers: *defaults
          """.trimIndent(),
          "yaml",
        )
      )
      .build()
      .loadConfigOrThrow<Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Worker(1234),
      "betaWorker" to Worker(20),
    )
  }
})
