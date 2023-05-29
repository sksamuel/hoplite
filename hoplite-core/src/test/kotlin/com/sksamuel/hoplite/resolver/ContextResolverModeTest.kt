package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class ContextResolverModeTest : FunSpec({

  data class Config(val bar: String)

  test("should error for unresolved substitutions when context resolver mode is ErrorOnUnresolved") {

    val props = Properties()
    props["bar"] = "\${{ ref:foo }}"

    shouldThrowAny {
      ConfigLoaderBuilder.newBuilder()
        .addPropertySource(PropsPropertySource(props))
        .withContextResolverMode(ContextResolverMode.ErrorOnUnresolved)
        .build()
        .loadConfigOrThrow<Config>()
    }.message shouldInclude "Could not resolve 'foo'"
  }

  test("should not error for unresolved substitutions when context resolver mode is SkipUnresolved") {

    val props = Properties()
    props["bar"] = "\${{ ref:foo }}"

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .withContextResolverMode(ContextResolverMode.SkipUnresolved)
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(bar = "\${{ ref:foo }}")
  }
})
