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

})
