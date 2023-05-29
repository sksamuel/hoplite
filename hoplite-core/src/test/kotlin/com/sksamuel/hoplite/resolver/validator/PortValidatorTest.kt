package com.sksamuel.hoplite.resolver.validator

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class PortValidatorTest : FunSpec({

  data class Config(val port: Int)

  test("port validation for valid port") {

    val props = Properties()
    props["port"] = 123

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .addResolver(PortValidator())
      .build()
      .loadConfigOrThrow<Config>()

    config.port shouldBe 123
  }

  test("port validation for invalid port") {

    val props = Properties()
    props["port"] = 13123123L

    shouldThrowAny {
      ConfigLoaderBuilder.newBuilder()
        .addPropertySource(PropsPropertySource(props))
        .addResolver(PortValidator())
        .build()
        .loadConfigOrThrow<Config>()
    }.message shouldInclude "Invalid port: 13123123 at (props)"
  }
})
