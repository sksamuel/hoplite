package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.util.Properties

// gh-602: in preprocessor mode, unresolved substitutions are only reported for values that are decoded,
// so shell variables the environment source imports (PS1, BASH_FUNC_*%%, ...) can't fail the load.
class Issue602Test : FunSpec({

  data class Cfg(val host: String)
  data class IntCfg(val port: Int)
  data class Nested(val url: String)
  data class NestedCfg(val db: Nested)
  data class ListCfg(val hosts: List<String>)
  data class MapCfg(val labels: Map<String, String>)
  data class ListOfDataCfg(val dbs: List<Nested>)
  data class PropsCfg(val props: Properties)

  val shellEnv = mapOf(
    "PS1" to "\${debian_chroot:+(\$debian_chroot)}\\u@\\h:\\w\\$ ",
    "BASH_FUNC_ml%%" to "() {  eval \"\$(\${LMOD_DIR}/ml_cmd \"\$@\")\" }",
  )

  fun builder(vararg values: Pair<String, String>) = ConfigLoaderBuilder.defaultWithoutPropertySources()
    .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { shellEnv }))
    .addMapSource(mapOf(*values))

  test("shell variables in the environment do not fail the load") {
    builder("host" to "localhost").build().loadConfigOrThrow<Cfg>() shouldBe Cfg("localhost")
  }

  test("shell variables in the environment do not fail the load in strict mode") {
    builder("host" to "localhost").strict().build().loadConfigOrThrow<Cfg>() shouldBe Cfg("localhost")
  }

  test("an unresolved substitution in a decoded string is reported") {
    shouldThrow<ConfigException> {
      builder("host" to "\${NOPE_602}").build().loadConfigOrThrow<Cfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a non-string field is reported in place of the decode error") {
    shouldThrow<ConfigException> {
      builder("port" to "\${NOPE_602}").build().loadConfigOrThrow<IntCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a nested data class field is reported") {
    shouldThrow<ConfigException> {
      builder("db.url" to "\${NOPE_602}").build().loadConfigOrThrow<NestedCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a list element is reported") {
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = {
          shellEnv + mapOf("HOSTS_0" to "a", "HOSTS_1" to "\${NOPE_602}")
        }))
        .build()
        .loadConfigOrThrow<ListCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a list of data classes is reported") {
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = {
          shellEnv + mapOf("DBS_0_URL" to "a", "DBS_1_URL" to "\${NOPE_602}")
        }))
        .build()
        .loadConfigOrThrow<ListOfDataCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a map value is reported") {
    shouldThrow<ConfigException> {
      builder("labels.a" to "x", "labels.b" to "\${NOPE_602}").build().loadConfigOrThrow<MapCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a Properties value is reported") {
    shouldThrow<ConfigException> {
      builder("props.a" to "x", "props.b" to "\${NOPE_602}").build().loadConfigOrThrow<PropsCfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("an unresolved substitution in a value the class does not bind is ignored") {
    builder("host" to "localhost", "stale" to "\${NOPE_602}").build().loadConfigOrThrow<Cfg>() shouldBe
      Cfg("localhost")
  }

  test("in strict mode, a value the class does not bind is reported as unused, not unresolved") {
    val message = shouldThrow<ConfigException> {
      builder("host" to "localhost", "stale" to "\${NOPE_602}").strict().build().loadConfigOrThrow<Cfg>()
    }.message
    message shouldContain "stale"
    message shouldNotContain "Unresolved substitution"
  }

  test("a config binder only reports unresolved substitutions under the bound prefix") {
    val binder = builder("a.host" to "\${NOPE_602}", "b.host" to "localhost").build().configBinder()
    binder.bindOrThrow<Cfg>("b") shouldBe Cfg("localhost")
    shouldThrow<ConfigException> {
      binder.bindOrThrow<Cfg>("a")
    }.message shouldContain "Unresolved substitution \${NOPE_602}"
  }

  test("allowUnresolvedSubstitutions still disables the check") {
    builder("host" to "\${NOPE_602}").allowUnresolvedSubstitutions().build().loadConfigOrThrow<Cfg>() shouldBe
      Cfg("\${NOPE_602}")
  }
})
