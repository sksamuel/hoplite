package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.Map
import io.vavr.kotlin.linkedHashMap

class MapDecoderTest : FunSpec({

  test("Map<String, String> decoded from yaml") {
    data class Test(val map: Map<String, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_map.yml")
    config shouldBe Test(linkedHashMap("key1" to "test1", "key2" to "test2", "key-3" to "test3", "Key4" to "test4"))
  }

})
