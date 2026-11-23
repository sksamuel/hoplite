package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addMapSource
import com.sksamuel.hoplite.secrets.SecretTypeSecretsPolicy
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.vavr.Tuple2
import io.vavr.collection.List
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.control.Option
import io.vavr.kotlin.linkedHashMap

@OptIn(ExperimentalHoplite::class)
class MapDecoderTest : FunSpec({
  data class Test(val map: Map<String, String>)

  test("Map<String, String> decoded from yaml") {
    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_map.yml")
    config shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

  test("Map<String, String> decoded from environment") {
    run {
      ConfigLoader {
        addPropertySource(EnvironmentVariablesPropertySource(
          environmentVariableMap = {
            mapOf(
              "map_key1" to "test1",
              "map_key2" to "test2",
              "map_key-3" to "test3",
              "map_Key4" to "test4",
            )
          }
        ))
      }.loadConfigOrThrow<Test>()
    } shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

  // Vavr MapDecoder used to forget context.usedPaths.add(v.path), so strict mode rejected every
  // entry of a vavr Map field as unused even though every entry was decoded.
  test("strict mode should accept all entries of a vavr Map") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("map.a" to "1", "map.b" to "2", "map.c" to "3"))
      .strict()
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(linkedHashMap("a" to "1", "b" to "2", "c" to "3"))
  }

  // gh-613: the report listed vavr Map entries as unused and printed collections of secrets in plain text.
  test("report should mask vavr collections, options and tuples of secrets and not list map entries as unused") {
    data class Secrets(
      val map: Map<String, Secret>,
      val array: Map<String, Secret>,
      val list: List<Secret>,
      val set: Set<Secret>,
      val option: Option<Secret>,
      val tuple: Tuple2<String, Secret>,
    )

    val report = StringBuilder()
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        PropertySource.map(
          mapOf(
            "map" to mapOf("a" to "map-secret"),
            "array" to listOf(mapOf("key" to "b", "value" to "array-secret")),
            "list" to listOf("list-secret-1", "list-secret-2"),
            "set" to "set-secret-1,set-secret-2",
            "option" to "option-secret",
            "tuple" to listOf("tuple-plain", "tuple-secret"),
          )
        )
      )
      .withSecretsPolicy(SecretTypeSecretsPolicy)
      .withReport()
      .withReportPrintFn { report.append(it) }
      .strict()
      .build()
      .loadConfigOrThrow<Secrets>()

    report.toString() shouldContain "Unused keys: none"
    report.toString() shouldNotContain "map-secret"
    report.toString() shouldNotContain "array-secret"
    report.toString() shouldNotContain "list-secret"
    report.toString() shouldNotContain "set-secret"
    report.toString() shouldNotContain "option-secret"
    report.toString() shouldNotContain "tuple-secret"
  }
})
