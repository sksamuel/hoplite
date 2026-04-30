package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class Gh472ReproTest : FunSpec({

  data class Cfg(val bootstrapServers: String)

  // gh-472: when the env var is missing the fallback should be returned, not the raw expression.
  // The reporter's tight syntax has no spaces around `:-`.
  test("env-var fallback should kick in when env var is missing (no spaces around :-)") {
    withEnvironment(emptyMap()) {
      val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
        .addMapSource(mapOf("bootstrapServers" to "\${{ env:BOOTSTRAPSERVERS_GH472:-fallback }}"))
        .build()
        .loadConfigOrThrow<Cfg>()

      cfg.bootstrapServers shouldBe "fallback"
    }
  }

  test("env-var fallback should kick in when env var is missing (with spaces around :-)") {
    withEnvironment(emptyMap()) {
      val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
        .addMapSource(mapOf("bootstrapServers" to "\${{ env:BOOTSTRAPSERVERS_GH472 :- fallback }}"))
        .build()
        .loadConfigOrThrow<Cfg>()

      cfg.bootstrapServers shouldBe "fallback"
    }
  }

  test("env-var resolution still works when the env var is set") {
    withEnvironment(mapOf("BOOTSTRAPSERVERS_GH472" to "real-value")) {
      val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
        .addMapSource(mapOf("bootstrapServers" to "\${{ env:BOOTSTRAPSERVERS_GH472:-fallback }}"))
        .build()
        .loadConfigOrThrow<Cfg>()

      cfg.bootstrapServers shouldBe "real-value"
    }
  }

  // Even if a user is still on the legacy preprocessor pipeline, the EnvOrSystemPropertyPreprocessor
  // and LookupPreprocessor must not mangle context-resolver syntax (`${{...}}`). Before the fix,
  // EnvOrSystemPropertyPreprocessor's `${(.*?)}` regex greedily consumed the inner `{` and the first
  // `}`, replacing `${{ env:VAR:-fallback }}` with `fallback }` — silently corrupting the value
  // before any resolver got a chance.
  test("legacy preprocessors must not mangle \${{...}} context-resolver syntax") {
    withEnvironment(emptyMap()) {
      val raw = "\${{ env:BOOTSTRAPSERVERS_GH472:-fallback }}"
      val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addMapSource(mapOf("bootstrapServers" to raw))
        .allowUnresolvedSubstitutions()
        .build()
        .loadConfigOrThrow<Cfg>()

      cfg.bootstrapServers shouldBe raw
    }
  }
})
