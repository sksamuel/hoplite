package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addMapSource
import com.sksamuel.hoplite.env.Environment
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class HopliteContextResolverTest : FunSpec({

  // Before the fix this returned `Runtime.getRuntime().availableProcessors()`, so the value of
  // the placeholder was the CPU count of the host running the loader — completely unrelated to
  // the Environment supplied to the config loader.
  test("\${{ hoplite:env }} resolves to the configured environment name") {
    data class Cfg(val region: String)

    val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .withEnvironment(Environment.prod)
      .addMapSource(mapOf("region" to "deploy-\${{ hoplite:env }}"))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.region shouldBe "deploy-prod"
  }

  test("\${{ hoplite:environment :- default }} falls back when no environment is configured") {
    data class Cfg(val region: String)

    val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .addMapSource(mapOf("region" to "deploy-\${{ hoplite:environment :- local }}"))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.region shouldBe "deploy-local"
  }
})
