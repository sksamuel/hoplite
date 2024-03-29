package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.Map
import io.vavr.kotlin.linkedHashMap

class MapDecoderTest : FunSpec({
  data class Test(val map: Map<String, String>)

  test("Map<String, String> decoded from yaml") {
    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_map.yml")
    config shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

  test("Map<String, String> decoded from environment with underscores") {
    run {
      ConfigLoader {
        addPropertySource(EnvironmentVariablesPropertySource(
          useUnderscoresAsSeparator = true,
          useSingleUnderscoresAsSeparator = false,
          allowUppercaseNames = true,
          environmentVariableMap = {
            mapOf(
              "map__key1" to "test1",
              "map__key2" to "test2",
              "map__key-3" to "test3",
              "map__Key4" to "test4",
            )
          }
        ))
      }.loadConfigOrThrow<Test>()
    } shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

  test("Map<String, String> decoded from environment") {
    run {
      ConfigLoader {
        addPropertySource(EnvironmentVariablesPropertySource(
          useUnderscoresAsSeparator = false,
          useSingleUnderscoresAsSeparator = false,
          allowUppercaseNames = true,
          environmentVariableMap = {
            mapOf(
              "map.key1" to "test1",
              "map.key2" to "test2",
              "map.key-3" to "test3",
              "map.Key4" to "test4",
            )
          }
        ))
      }.loadConfigOrThrow<Test>()
    } shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

})
