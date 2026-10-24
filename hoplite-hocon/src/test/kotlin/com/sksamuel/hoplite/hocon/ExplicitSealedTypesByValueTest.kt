package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.addMapSource
import com.sksamuel.hoplite.parsers.PropsPropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.Properties

sealed interface Selected {
  data class Foo(val x: String) : Selected
  data class Defaults(val x: String = "default", val y: Int = 1) : Selected
  data object Bar : Selected
}

data class SelectedConfig(val sealedValue: Selected)

/**
 * In explicit sealed types mode, a node that has both its own string value and children selects the subtype
 * by that value, which is how flat key sources such as env vars select a subtype without a discriminator.
 */
@OptIn(ExperimentalHoplite::class)
class ExplicitSealedTypesByValueTest : FunSpec({

  fun env(vararg vars: Pair<String, String>) =
    EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf(*vars) })

  fun hocon(str: String) = PropertySource.string(str, "conf")

  fun load(vararg sources: PropertySource, strict: Boolean = false): SelectedConfig =
    ConfigLoaderBuilder.newBuilder()
      .withExplicitSealedTypes()
      .addPropertySources(sources.toList())
      .apply { if (strict) strict() }
      .build()
      .loadConfigOrThrow<SelectedConfig>()

  test("env var value selects a data class subtype, and its children are the subtype's properties") {
    load(env("SEALEDVALUE" to "Foo", "SEALEDVALUE_X" to "abc")) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value selects an object subtype") {
    load(env("SEALEDVALUE" to "Bar")) shouldBe SelectedConfig(Selected.Bar)
  }

  test("env var value alone selects a data class subtype constructed from its defaults") {
    load(env("SEALEDVALUE" to "Defaults")) shouldBe SelectedConfig(Selected.Defaults())
  }

  test("hocon value alone selects a data class subtype constructed from its defaults") {
    load(hocon("sealedValue = Defaults")) shouldBe SelectedConfig(Selected.Defaults())
  }

  test("env var value alone for a data class subtype with a required parameter should error") {
    shouldThrow<ConfigException> {
      load(env("SEALEDVALUE" to "Foo"))
    }.message shouldContain "- 'x': Missing String from config"
  }

  test("env var value naming no subtype should error, listing the available subtypes") {
    shouldThrow<ConfigException> {
      load(env("SEALEDVALUE" to "Nope", "SEALEDVALUE_X" to "abc"))
    }.message shouldContain "matching name Nope: Available Bar, Defaults, Foo"
  }

  test("hocon discriminator field selects the subtype") {
    load(hocon("sealedValue { _type = Foo, x = abc }")) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var overrides a property of a subtype selected by a hocon discriminator field") {
    load(
      env("SEALEDVALUE_X" to "abc"),
      hocon("sealedValue { _type = Foo, x = xyz }"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value naming the same subtype as a hocon discriminator field") {
    load(
      env("SEALEDVALUE" to "Foo", "SEALEDVALUE_X" to "abc"),
      hocon("sealedValue { _type = Foo, x = xyz }"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value overrides the subtype selected by a hocon discriminator field") {
    load(
      env("SEALEDVALUE" to "Foo", "SEALEDVALUE_X" to "abc"),
      hocon("sealedValue { _type = Bar }"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value overrides the subtype selected by a hocon discriminator field with an object") {
    load(
      env("SEALEDVALUE" to "Bar"),
      hocon("sealedValue { _type = Foo, x = xyz }"),
    ) shouldBe SelectedConfig(Selected.Bar)
  }

  test("properties set for the overridden subtype merge into the selected subtype where their names match") {
    load(
      env("SEALEDVALUE" to "Defaults", "SEALEDVALUE_Y" to "2"),
      hocon("sealedValue { _type = Foo, x = xyz }"),
    ) shouldBe SelectedConfig(Selected.Defaults(x = "xyz", y = 2))
  }

  test("hocon discriminator field overrides the subtype selected by a lower priority env var value") {
    load(
      hocon("sealedValue { _type = Foo, x = abc }"),
      env("SEALEDVALUE" to "Bar"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("hocon discriminator field overrides the subtype selected by a lower priority env var value with children") {
    load(
      hocon("sealedValue { _type = Bar }"),
      env("SEALEDVALUE" to "Foo", "SEALEDVALUE_X" to "abc"),
    ) shouldBe SelectedConfig(Selected.Bar)
  }

  test("env var value selects the subtype whose properties a hocon map without a discriminator field sets") {
    load(
      env("SEALEDVALUE" to "Foo"),
      hocon("sealedValue { x = abc }"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value naming the same subtype as a hocon discriminator field keeps the hocon properties") {
    load(
      env("SEALEDVALUE" to "Foo"),
      hocon("sealedValue { _type = Foo, x = abc }"),
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("properties set by a lower priority value selecting another subtype merge into the selected subtype") {
    val props = Properties().apply {
      setProperty("sealedValue", "Foo")
      setProperty("sealedValue.x", "fromProps")
    }
    load(
      env("SEALEDVALUE" to "Defaults", "SEALEDVALUE_Y" to "2"),
      PropsPropertySource(props),
    ) shouldBe SelectedConfig(Selected.Defaults(x = "fromProps", y = 2))
  }

  test("object subtype selected by value ignores children when not strict") {
    load(env("SEALEDVALUE" to "Bar", "SEALEDVALUE_X" to "abc")) shouldBe SelectedConfig(Selected.Bar)
  }

  fun loadStrict(vararg sources: Map<String, Any>): SelectedConfig =
    ConfigLoaderBuilder.newBuilder()
      .withExplicitSealedTypes()
      .apply { sources.forEach { addMapSource(it) } }
      .strict()
      .build()
      .loadConfigOrThrow<SelectedConfig>()

  // env vars are never reported as unused by strict mode, so these use map sources with the same flat key shape

  test("object subtype selected by value reports children as unused when strict") {
    shouldThrow<ConfigException> {
      loadStrict(mapOf("sealedValue" to "Bar", "sealedValue.x" to "abc"))
    }.message shouldContain "sealedValue.x"
  }

  test("properties a lower priority source sets for another subtype are reported as unused when strict") {
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.newBuilder()
        .withExplicitSealedTypes()
        .addMapSource(mapOf("sealedValue" to "Bar"))
        .addPropertySource(hocon("sealedValue { _type = Foo, x = xyz }"))
        .strict()
        .build()
        .loadConfigOrThrow<SelectedConfig>()
    }.message shouldContain "sealedValue.x"
  }

  test("data class subtype selected by value is fine when strict") {
    loadStrict(mapOf("sealedValue" to "Foo", "sealedValue.x" to "abc")) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("java properties value selects the subtype") {
    val props = Properties().apply {
      setProperty("sealedValue", "Foo")
      setProperty("sealedValue.x", "abc")
    }
    load(PropsPropertySource(props)) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("system property value selects the subtype") {
    val sysprops = SystemPropertiesPropertySource(
      systemPropertiesMap = { mapOf("config.override.sealedValue" to "Foo", "config.override.sealedValue.x" to "abc") }
    )
    load(sysprops) shouldBe SelectedConfig(Selected.Foo("abc"))
  }
})
