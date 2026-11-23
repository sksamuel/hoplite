package com.sksamuel.hoplite.arrow

import arrow.core.nonEmptyListOf
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.secrets.SecretTypeSecretsPolicy
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

@OptIn(ExperimentalHoplite::class)
class NonEmptyListTest : FunSpec({

  test("NonEmptyList<A> as delimited string") {
    data class Test(val strings: arrow.core.NonEmptyList<String>, val longs: arrow.core.NonEmptyList<Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_nel.yml")
    config shouldBe Test(nonEmptyListOf("1", "2", "a", "b"), nonEmptyListOf(1L, 2L, 3L, 4L))
  }

  // Per-element decoders must be invoked with the element KType, not the outer
  // NonEmptyList<T> KType, otherwise nested decoders that read type.arguments break.
  test("NonEmptyList<DataClass> passes element type to data class decoder") {
    data class Item(val name: String, val score: Int)
    data class Test(val items: arrow.core.NonEmptyList<Item>)

    val config = ConfigLoaderBuilder.default()
      .addPropertySource(
        YamlPropertySource(
          """
            items:
              - name: alice
                score: 1
              - name: bob
                score: 2
          """
        )
      )
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(nonEmptyListOf(Item("alice", 1), Item("bob", 2)))
  }

  test("NonEmptyList<Map<String, Int>> passes element type to map decoder") {
    data class Test(val items: arrow.core.NonEmptyList<Map<String, Int>>)

    val config = ConfigLoaderBuilder.default()
      .addPropertySource(
        YamlPropertySource(
          """
            items:
              - a: 1
                b: 2
              - c: 3
          """
        )
      )
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(nonEmptyListOf(mapOf("a" to 1, "b" to 2), mapOf("c" to 3)))
  }

  // gh-613: the report printed the elements of a NonEmptyList<Secret> in plain text.
  test("report should mask the elements of a NonEmptyList<Secret>") {
    data class Test(val array: arrow.core.NonEmptyList<Secret>, val csv: arrow.core.NonEmptyList<Secret>)

    val report = StringBuilder()
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        PropertySource.map(
          mapOf("array" to listOf("array-secret-1", "array-secret-2"), "csv" to "csv-secret-1,csv-secret-2")
        )
      )
      .withSecretsPolicy(SecretTypeSecretsPolicy)
      .withReport()
      .withReportPrintFn { report.append(it) }
      .build()
      .loadConfigOrThrow<Test>()

    report.toString() shouldContain "Unused keys: none"
    report.toString() shouldNotContain "array-secret"
    report.toString() shouldNotContain "csv-secret"
  }

})
