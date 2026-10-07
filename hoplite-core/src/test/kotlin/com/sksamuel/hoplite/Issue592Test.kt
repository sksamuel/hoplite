package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

data class Issue592DatabaseConfig(val host: String)
data class Issue592Config(val host: String, val database: Issue592DatabaseConfig)

class Issue592Test : FunSpec({

  // gh-592: an environment variable such as HOST_FOO makes the env property source expose
  // `host` as a map. Being higher precedence than the file, that map used to completely
  // override the file's scalar `host`, discarding its value — so decoding `host: String`
  // failed with "Missing String from config", even though the file clearly defined it.
  // A nested `database.host` was unaffected, and renaming the root key made it work, which
  // is exactly what the reporter observed.
  test("a file scalar is not discarded when an env var turns the same key into a map") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      // higher precedence: turns `host` into a map { foo = bar }
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("HOST_FOO" to "bar") }))
      // lower precedence: defines `host` and `database.host` as scalars
      .addPropertySource(
        PropertySource.string(
          """
          host=0.0.0.0
          database.host=0.0.0.0
          """.trimIndent(),
          "props",
        )
      )
      .build()
      .loadConfigOrThrow<Issue592Config>()

    config.host shouldBe "0.0.0.0"
    config.database.host shouldBe "0.0.0.0"
  }
})
