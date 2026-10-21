package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * Sealed subtype names are matched case-sensitively, unless [ConfigLoaderBuilder.withResolveTypesCaseInsensitive]
 * is set, as for enum constant names.
 */
@OptIn(ExperimentalHoplite::class)
class SealedTypesCaseInsensitiveTest : FunSpec({

  fun env(vararg vars: Pair<String, String>) =
    EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf(*vars) })

  fun hocon(str: String) = PropertySource.string(str, "conf")

  fun load(vararg sources: PropertySource, explicit: Boolean = true, ignoreCase: Boolean): SelectedConfig =
    ConfigLoaderBuilder.newBuilder()
      .apply { if (explicit) withExplicitSealedTypes() }
      .apply { if (ignoreCase) withResolveTypesCaseInsensitive() }
      .addPropertySources(sources.toList())
      .build()
      .loadConfigOrThrow<SelectedConfig>()

  test("env var value selects a subtype ignoring case when case-insensitive") {
    load(env("SEALEDVALUE" to "foo", "SEALEDVALUE_X" to "abc"), ignoreCase = true) shouldBe
      SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value selects an object subtype ignoring case when case-insensitive") {
    load(env("SEALEDVALUE" to "BAR"), ignoreCase = true) shouldBe SelectedConfig(Selected.Bar)
  }

  test("env var value naming a subtype in the wrong case should error when case-sensitive") {
    shouldThrow<ConfigException> {
      load(env("SEALEDVALUE" to "foo", "SEALEDVALUE_X" to "abc"), ignoreCase = false)
    }.message shouldContain "matching name foo: Available Bar, Defaults, Foo"
  }

  test("discriminator field selects a subtype ignoring case when case-insensitive") {
    load(hocon("sealedValue { _type = foo, x = abc }"), ignoreCase = true) shouldBe
      SelectedConfig(Selected.Foo("abc"))
  }

  test("discriminator field naming a subtype in the wrong case should error when case-sensitive") {
    shouldThrow<ConfigException> {
      load(hocon("sealedValue { _type = foo, x = abc }"), ignoreCase = false)
    }.message shouldContain "found using the discriminator value `foo`"
  }

  test("env var value overrides a hocon discriminator field ignoring case when case-insensitive") {
    load(
      env("SEALEDVALUE" to "foo", "SEALEDVALUE_X" to "abc"),
      hocon("sealedValue { _type = Bar }"),
      ignoreCase = true,
    ) shouldBe SelectedConfig(Selected.Foo("abc"))
  }

  test("env var value in the wrong case overriding a hocon discriminator field should error when case-sensitive") {
    shouldThrow<ConfigException> {
      load(
        env("SEALEDVALUE" to "foo", "SEALEDVALUE_X" to "abc"),
        hocon("sealedValue { _type = Foo, x = xyz }"),
        ignoreCase = false,
      )
    }.message shouldContain "matching name foo: Available Bar, Defaults, Foo"
  }

  test("env var value and a lower priority hocon discriminator field naming the same subtype in different cases merge when case-insensitive") {
    load(
      env("SEALEDVALUE" to "foo"),
      hocon("sealedValue { _type = FOO, x = xyz }"),
      ignoreCase = true,
    ) shouldBe SelectedConfig(Selected.Foo("xyz"))
  }

  test("discriminator fields of two sources naming the same subtype in different cases merge when case-insensitive") {
    load(
      hocon("sealedValue { _type = foo }"),
      hocon("sealedValue { _type = FOO, x = xyz }"),
      ignoreCase = true,
    ) shouldBe SelectedConfig(Selected.Foo("xyz"))
  }

  test("discriminator fields of two sources naming the same subtype in different cases should error when case-sensitive") {
    shouldThrow<ConfigException> {
      load(
        hocon("sealedValue { _type = foo }"),
        hocon("sealedValue { _type = Foo, x = xyz }"),
        ignoreCase = false,
      )
    }.message shouldContain "found using the discriminator value `foo`"
  }

  test("object name selects an object subtype ignoring case when case-insensitive, without explicit sealed types") {
    load(hocon("sealedValue = bar"), explicit = false, ignoreCase = true) shouldBe SelectedConfig(Selected.Bar)
  }

  test("object name in the wrong case should error when case-sensitive, without explicit sealed types") {
    shouldThrow<ConfigException> {
      load(hocon("sealedValue = bar"), explicit = false, ignoreCase = false)
    }.message shouldContain "Could not find appropriate subclass"
  }
})
