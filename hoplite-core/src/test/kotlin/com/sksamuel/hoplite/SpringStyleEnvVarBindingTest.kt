package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Verifies that hoplite supports the same env-var binding rules Spring Boot documents at:
 * https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables
 *
 * Spring's three core rules for converting a property path to an env var:
 *   1. Replace dots (`.`) with underscores (`_`)
 *   2. Remove dashes (`-`)
 *   3. Convert to uppercase
 *
 * Plus a list/array rule:
 *   - List/array indices are surrounded by underscores: `my.service[0].other` ↔ `MY_SERVICE_0_OTHER`
 *
 * And a map rule:
 *   - Trailing path segment names a map key (lowercased): `MY_PROPS_VALUES_KEY=value` produces
 *     `{"key": "value"}`
 */
class SpringStyleEnvVarBindingTest : FunSpec({

  fun loader(env: Map<String, String>) = ConfigLoaderBuilder.defaultWithoutPropertySources()
    .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))
    .build()

  // ---------------------------------------------------------------------------------------------
  // Rule 1: dots → underscores. Camel-cased Kotlin field name binds to a flat uppercase env var.
  // Spring example: `spring.main.logStartupInfo` ↔ `SPRING_MAIN_LOGSTARTUPINFO`
  // ---------------------------------------------------------------------------------------------
  test("uppercase env var binds to camelCase data class field via dots-as-underscores") {
    data class Main(val logStartupInfo: Boolean)
    data class Spring(val main: Main)
    data class Root(val spring: Spring)

    val cfg = loader(mapOf("SPRING_MAIN_LOGSTARTUPINFO" to "true"))
      .loadConfigOrThrow<Root>()

    cfg shouldBe Root(Spring(Main(logStartupInfo = true)))
  }

  // ---------------------------------------------------------------------------------------------
  // Rule 2: dashes are removed (NOT replaced with underscores).
  // Spring's example specifically targets `log-startup-info` collapsing to `LOGSTARTUPINFO`.
  // ---------------------------------------------------------------------------------------------
  test("kebab-case property path matches a flat uppercase env var (dashes removed)") {
    data class Cfg(@ConfigAlias("log-startup-info") val flag: Boolean)

    val cfg = loader(mapOf("LOGSTARTUPINFO" to "true"))
      .loadConfigOrThrow<Cfg>()

    cfg.flag shouldBe true
  }

  // ---------------------------------------------------------------------------------------------
  // Rule 3: case-insensitive matching against the property path.
  // ---------------------------------------------------------------------------------------------
  test("uppercase env var matches lowercase Kotlin field") {
    data class Cfg(val name: String)

    val cfg = loader(mapOf("NAME" to "hoplite"))
      .loadConfigOrThrow<Cfg>()

    cfg.name shouldBe "hoplite"
  }

  // ---------------------------------------------------------------------------------------------
  // Array / list binding via numeric segments (Spring's `MY_SERVICE_0_OTHER` rule).
  // ---------------------------------------------------------------------------------------------
  test("list of primitives binds via numeric env var segments") {
    data class Cfg(val items: List<String>)

    val cfg = loader(
      mapOf(
        "ITEMS_0" to "alpha",
        "ITEMS_1" to "beta",
        "ITEMS_2" to "gamma",
      )
    ).loadConfigOrThrow<Cfg>()

    cfg.items shouldBe listOf("alpha", "beta", "gamma")
  }

  test("list of nested objects binds via numeric env var segments — Spring's MY_SERVICE_0_OTHER example") {
    data class Service(val other: String)
    data class Cfg(val service: List<Service>)

    val cfg = loader(
      mapOf(
        "SERVICE_0_OTHER" to "first",
        "SERVICE_1_OTHER" to "second",
      )
    ).loadConfigOrThrow<Cfg>()

    cfg.service shouldBe listOf(Service("first"), Service("second"))
  }

  // ---------------------------------------------------------------------------------------------
  // Map binding (Spring's `MY_PROPS_VALUES_KEY=value` rule).
  //
  // KNOWN DIFFERENCE FROM SPRING: Spring lowercases the env var name before binding, so
  // `MY_PROPS_VALUES_KEY=VALUE` produces `{"key" = "VALUE"}` (lowercased key, preserved value).
  // Hoplite preserves the original case of map keys — there is an explicit
  // `EnvironmentVariablesPropertySourceTest > build env source can create case sensitive Maps`
  // that locks this behaviour in. So the tests below assert hoplite's case-preserving semantics.
  // ---------------------------------------------------------------------------------------------
  test("map of strings binds with the trailing segment as the key (case preserved)") {
    data class Cfg(val values: Map<String, String>)

    val cfg = loader(
      mapOf(
        "VALUES_FOO" to "1",
        "VALUES_BAR" to "2",
      )
    ).loadConfigOrThrow<Cfg>()

    // Spring would produce {"foo" = "1", "bar" = "2"}.
    cfg.values shouldBe mapOf("FOO" to "1", "BAR" to "2")
  }

  test("map values are preserved verbatim regardless of map key case") {
    data class Cfg(val values: Map<String, String>)

    val cfg = loader(mapOf("VALUES_KEY" to "VALUE"))
      .loadConfigOrThrow<Cfg>()

    // Spring would produce {"key" = "VALUE"}.
    cfg.values shouldBe mapOf("KEY" to "VALUE")
  }

  // ---------------------------------------------------------------------------------------------
  // Combined: nested object containing a list and a map.
  // ---------------------------------------------------------------------------------------------
  test("nested mix of list, map and scalars via env vars") {
    data class Service(val other: String, val tags: List<String>)
    data class Cfg(val service: List<Service>, val labels: Map<String, String>)

    val cfg = loader(
      mapOf(
        "SERVICE_0_OTHER" to "alpha",
        "SERVICE_0_TAGS_0" to "x",
        "SERVICE_0_TAGS_1" to "y",
        "SERVICE_1_OTHER" to "beta",
        "SERVICE_1_TAGS_0" to "z",
        "LABELS_ENV" to "prod",
        "LABELS_REGION" to "us",
      )
    ).loadConfigOrThrow<Cfg>()

    cfg shouldBe Cfg(
      service = listOf(
        Service("alpha", listOf("x", "y")),
        Service("beta", listOf("z")),
      ),
      // Map keys preserve the env var case — see the map tests above.
      labels = mapOf("ENV" to "prod", "REGION" to "us"),
    )
  }
})
