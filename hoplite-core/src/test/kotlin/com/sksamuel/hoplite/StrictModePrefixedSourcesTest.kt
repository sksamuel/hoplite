package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

// #606: by default (since 3.1), a source whose prefix gives it its own namespace is checked for unused
// values in strict mode, and only ambient sources (unprefixed env, sysprops with an empty prefix) stay exempt.
class StrictModePrefixedSourcesTest : FunSpec({

  data class Database(val url: String)
  data class TestConfig(val database: Database)

  fun env(vararg vars: Pair<String, String>, prefix: String? = null) =
    EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf(*vars) }, prefix = prefix)

  fun sysprops(vararg props: Pair<String, String>, prefix: String = SystemPropertiesPropertySource.DEFAULT_PREFIX) =
    SystemPropertiesPropertySource(systemPropertiesMap = { mapOf(*props) }, prefix = prefix)

  // Relies on the default unless strictPrefixed is given.
  fun builder(vararg sources: PropertySource, strictPrefixed: Boolean? = null) =
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .apply { sources.forEach { addPropertySource(it) } }
      .strict()
      .apply { strictPrefixed?.let { withStrictPrefixedSources(it) } }

  test("a typo in a prefixed env var is reported as unused") {
    val ex = shouldThrow<ConfigException> {
      builder(
        env("MYAPP_DATABASE_URL" to "jdbc:a", "MYAPP_DATABSE_URL" to "jdbc:b", "HOME" to "/root", prefix = "MYAPP_"),
      ).build().loadConfigOrThrow<TestConfig>()
    }
    ex.message
      .shouldContain("Config value 'DATABSE_URL' at (env) was unused")
      .shouldNotContain("DATABASE_URL")
      .shouldNotContain("HOME")
  }

  test("a prefixed env source with every var used passes") {
    builder(env("MYAPP_DATABASE_URL" to "jdbc:a", "HOME" to "/root", prefix = "MYAPP_"))
      .build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }

  test("unprefixed env vars stay exempt") {
    builder(env("DATABASE_URL" to "jdbc:a", "DATABSE_URL" to "jdbc:b", "HOME" to "/root"))
      .build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }

  test("unprefixed env vars stay exempt when merged with a file-like source") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("DATABASE_URL" to "jdbc:a", "HOME" to "/root", "PATH" to "/bin"))
      .addMapSource(mapOf("database.url" to "jdbc:b"))
      .strict()
      .build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }

  test("a typo in a system property under the default prefix is reported as unused") {
    val ex = shouldThrow<ConfigException> {
      builder(
        sysprops(
          "config.override.database.url" to "jdbc:a",
          "config.override.databse.url" to "jdbc:b",
          "java.version" to "21",
        ),
      ).build().loadConfigOrThrow<TestConfig>()
    }
    ex.message
      .shouldContain("Config value 'config.override.databse.url' at (sysprops) was unused")
      .shouldNotContain("java")
  }

  test("system properties with an empty prefix stay exempt") {
    builder(sysprops("database.url" to "jdbc:a", "java.version" to "21", "user.home" to "/root", prefix = ""))
      .build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }

  test("user-provided sources are still checked") {
    val ex = shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(env("HOME" to "/root"))
        .addMapSource(mapOf("database.url" to "jdbc:a", "stale" to "x"))
        .strict()
        .build().loadConfigOrThrow<TestConfig>()
    }
    ex.message.shouldContain("Config value 'stale'").shouldNotContain("HOME")
  }

  test("opting out exempts prefixed env vars and system properties again") {
    builder(
      env("MYAPP_DATABASE_URL" to "jdbc:a", "MYAPP_DATABSE_URL" to "jdbc:b", prefix = "MYAPP_"),
      sysprops("config.override.databse.host" to "x"),
      strictPrefixed = false,
    ).build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }

  test("prefixed sources are not checked in lenient mode") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("MYAPP_DATABASE_URL" to "jdbc:a", "MYAPP_DATABSE_URL" to "jdbc:b", prefix = "MYAPP_"))
      .build().loadConfigOrThrow<TestConfig>() shouldBe TestConfig(Database("jdbc:a"))
  }
})
