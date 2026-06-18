package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addMapSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class SystemPropertyContextResolverEmptyKeyTest : FunSpec({

  // System.getProperty(key) throws IllegalArgumentException when key is empty. The resolver
  // previously called it without guarding, so a placeholder like `${{ sysprop:  }}` (or one whose
  // key trimmed down to empty) crashed the loader instead of producing a clean failure.
  test("\${{ sysprop:  }} should not crash and is reported as unresolved") {
    data class Cfg(val a: String)
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.newBuilderWithoutPropertySources()
        .addMapSource(mapOf("a" to "\${{ sysprop:  }}"))
        .build()
        .loadConfigOrThrow<Cfg>()
    }
  }

  test("\${{ sysprop:somekey :- fallback }} for a missing property uses the fallback") {
    System.clearProperty("hoplite.unset.test.key")
    data class Cfg(val a: String)
    val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .addMapSource(mapOf("a" to "\${{ sysprop:hoplite.unset.test.key :- fallback }}"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.a shouldBe "fallback"
  }

  // A default value may itself contain ":-". The key/default split must happen on the FIRST ":-"
  // (lookup keys are property/env names and cannot contain ":-"), not the last. A greedy first
  // group split on the last ":-", so the fallback below resolved to "b" instead of "a:-b".
  test("\${{ sysprop:somekey :- default }} keeps a ':-' that appears in the default value") {
    System.clearProperty("hoplite.unset.test.key")
    data class Cfg(val a: String)
    val cfg = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .addMapSource(mapOf("a" to "\${{ sysprop:hoplite.unset.test.key :- a:-b }}"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.a shouldBe "a:-b"
  }
})
