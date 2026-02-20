package com.sksamuel.hoplite.resolver.validator

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.JdbcTestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class HostnameValidatorTest : FunSpec({

  install(JdbcTestContainerExtension(postgreSQLContainer))
  data class Config(val hostname: String)

  test("hostname validation for valid host") {

    val props = Properties()
    props["hostname"] = postgreSQLContainer.host

    val config = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
      .addPropertySource(PropsPropertySource(props))
      .addResolver(HostnameValidator())
      .build()
      .loadConfigOrThrow<Config>()

    config.hostname shouldBe postgreSQLContainer.host
  }

  test("hostname validation for invalid host") {

    val props = Properties()
    props["hostname"] = "invalidhostname"

    shouldThrowAny {
      ConfigLoaderBuilder.newBuilderWithoutPropertySources()
        .addPropertySource(PropsPropertySource(props))
        .addResolver(HostnameValidator())
        .build()
        .loadConfigOrThrow<Config>()
    }.message shouldInclude "Hostname `invalidhostname` not reachable at (props)"
  }
})
