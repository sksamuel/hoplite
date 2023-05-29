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
import org.testcontainers.containers.PostgreSQLContainer
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class JdbcHostnameValidatorTest : FunSpec({

  install(JdbcTestContainerExtension(postgreSQLContainer))
  data class Config(val url: String)

  test("jdbc validation for valid host") {

    val props = Properties()
    props["url"] = postgreSQLContainer.jdbcUrl

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .addResolver(JdbcHostnameValidator())
      .build()
      .loadConfigOrThrow<Config>()

    config.url shouldBe postgreSQLContainer.jdbcUrl
  }

  test("jdbc validation for invalid host") {

    val props = Properties()
    props["url"] = "jdbc:postgres://invalidhostname"

    shouldThrowAny {
      ConfigLoaderBuilder.newBuilder()
        .addPropertySource(PropsPropertySource(props))
        .addResolver(JdbcHostnameValidator())
        .build()
        .loadConfigOrThrow<Config>()
    }.message shouldInclude "JDBC hostname `invalidhostname` not reachable at (props)"
  }
})

val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:13.7").apply {
  withUsername("postgres")
  withPassword("letmein")
  startupAttempts = 1
  withUrlParam("connectionTimeZone", "Z")
  withUrlParam("zeroDateTimeBehavior", "convertToNull")
}
