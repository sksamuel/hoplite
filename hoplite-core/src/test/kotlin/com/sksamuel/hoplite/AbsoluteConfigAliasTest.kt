package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesLookupPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

// gh-368: @ConfigAlias(name, absolute = true) reads one specific env var or system property, wherever the field is
class AbsoluteConfigAliasTest : FunSpec({

  data class SomeOptions(@ConfigAlias("ENV_VAL", absolute = true) val string: String)
  data class Config(val options: SomeOptions)

  fun builder(env: Map<String, String>) = ConfigLoaderBuilder.defaultWithoutPropertySources()
    .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))

  fun loader(env: Map<String, String>) = builder(env).build()

  test("a nested field reads the env var its absolute alias names") {
    loader(mapOf("ENV_VAL" to "a")).loadConfigOrThrow<Config>() shouldBe Config(SomeOptions("a"))
  }

  test("the path-based name wins over the absolute alias") {
    loader(mapOf("ENV_VAL" to "a", "OPTIONS_STRING" to "b"))
      .loadConfigOrThrow<Config>() shouldBe Config(SomeOptions("b"))
  }

  test("an absolute alias isn't a name relative to the parent") {
    shouldThrow<ConfigException> {
      loader(mapOf("OPTIONS_ENV_VAL" to "a")).loadConfigOrThrow<Config>()
    }.message shouldContain "options"
  }

  test("an absolute alias ignores the config binder's prefix") {
    val binder = loader(mapOf("ENV_VAL" to "a")).configBinder()
    binder.bindOrThrow<SomeOptions>("options") shouldBe SomeOptions("a")
    binder.bindOrThrow<SomeOptions>("some.other.place") shouldBe SomeOptions("a")
  }

  test("an absolute alias overrides a file registered after the env vars, as any env var does") {
    builder(mapOf("ENV_VAL" to "a"))
      .addMapSource(mapOf("options" to mapOf("string" to "file")))
      .build()
      .loadConfigOrThrow<Config>() shouldBe Config(SomeOptions("a"))
  }

  test("several absolute aliases are tried in order") {
    data class Cfg(
      @ConfigAlias("NEW_NAME", absolute = true) @ConfigAlias("OLD_NAME", absolute = true) val value: String,
    )
    loader(mapOf("OLD_NAME" to "old")).loadConfigOrThrow<Cfg>() shouldBe Cfg("old")
    loader(mapOf("OLD_NAME" to "old", "NEW_NAME" to "new")).loadConfigOrThrow<Cfg>() shouldBe Cfg("new")
  }

  test("a path-based value for any field of a data class wins over the data class's absolute alias as a whole") {
    data class Db(val url: String, val maxPoolSize: Int = 1)
    data class Cfg(@ConfigAlias("LEGACY_DB", absolute = true) val db: Db)
    loader(mapOf("DB_URL" to "new", "LEGACY_DB_URL" to "old", "LEGACY_DB_MAXPOOLSIZE" to "3"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Db("new", 1))
  }

  test("an absolute alias on a data class field reads its fields under the absolute name") {
    data class Db(val url: String, val maxPoolSize: Int)
    data class Cfg(@ConfigAlias("LEGACY_DB", absolute = true) val db: Db)
    loader(mapOf("LEGACY_DB_URL" to "jdbc:x", "LEGACY_DB_MAXPOOLSIZE" to "3"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Db("jdbc:x", 3))
  }

  test("an absolute alias alongside a relative alias") {
    data class Cfg(@ConfigAlias("timeoutMs") @ConfigAlias("APP_TIMEOUT", absolute = true) val timeout: Int)
    loader(mapOf("APP_TIMEOUT" to "1")).loadConfigOrThrow<Cfg>() shouldBe Cfg(1)
    loader(mapOf("APP_TIMEOUT" to "1", "TIMEOUTMS" to "2")).loadConfigOrThrow<Cfg>() shouldBe Cfg(2)
  }

  test("a relative alias binds as before") {
    data class Db(@ConfigAlias("host") val hostname: String)
    data class Cfg(val db: Db)
    loader(mapOf("DB_HOST" to "h")).loadConfigOrThrow<Cfg>() shouldBe Cfg(Db("h"))
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("db" to mapOf("host" to "h")))
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Db("h"))
  }

  test("tree sources ignore absolute aliases") {
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addMapSource(mapOf("ENV_VAL" to "a", "options" to mapOf("ENV_VAL" to "a")))
        .build()
        .loadConfigOrThrow<Config>()
    }
  }

  test("prefixed env vars ignore absolute aliases") {
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(EnvironmentVariablesPropertySource(prefix = "APP_", environmentVariableMap = {
          mapOf("APP_ENV_VAL" to "a", "ENV_VAL" to "a")
        }))
        .build()
        .loadConfigOrThrow<Config>()
    }
  }

  test("with the tree-based env source (withAmbientSourceLookup(false)) absolute aliases don't apply") {
    shouldThrow<ConfigException> {
      builder(mapOf("ENV_VAL" to "a")).withAmbientSourceLookup(false).build().loadConfigOrThrow<Config>()
    }
    builder(mapOf("OPTIONS_STRING" to "b")).withAmbientSourceLookup(false).build()
      .loadConfigOrThrow<Config>() shouldBe Config(SomeOptions("b"))
  }

  test("system properties with an empty prefix read an absolute alias") {
    data class Http(@ConfigAlias("legacy.http.timeout", absolute = true) val timeout: Int)
    data class Cfg(val http: Http)
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(SystemPropertiesLookupPropertySource { mapOf("legacy.http.timeout" to "5") })
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Http(5))
    try {
      System.setProperty("legacy368.http.timeout", "6")
      data class Http2(@ConfigAlias("legacy368.http.timeout", absolute = true) val timeout: Int)
      data class Cfg2(val http: Http2)
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(SystemPropertiesPropertySource(prefix = ""))
        .build()
        .loadConfigOrThrow<Cfg2>() shouldBe Cfg2(Http2(6))
    } finally {
      System.clearProperty("legacy368.http.timeout")
    }
  }

  test("the report shows the env var the value came from") {
    val output = StringBuilder()
    builder(mapOf("ENV_VAL" to "a"))
      .withReport()
      .withReportPrintFn { output.append(it) }
      .build()
      .loadConfigOrThrow<Config>()
    val report = output.toString()
    report shouldContain "ENV_VAL"
    report shouldContain "Env Var (lookup)"
  }
})
