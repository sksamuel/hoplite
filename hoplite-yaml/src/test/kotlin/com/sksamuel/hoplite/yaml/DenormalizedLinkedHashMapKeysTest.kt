package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceOrFileSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DenormalizedLinkedHashMapKeysTest : FunSpec({
  data class Foo(
    val xVal: String = "x"
  )

  data class LinkedHashMapContainer(
    val m: LinkedHashMap<String, Foo> = linkedMapOf()
  )

  test("should set denormalized map keys and decode a data class inside a linked has map map") {
    val config = ConfigLoaderBuilder.default()
      .addResourceOrFileSource("/test_data_class_in_map.yaml")
      .build()
      .loadConfigOrThrow<LinkedHashMapContainer>()

    config shouldBe LinkedHashMapContainer(
      m = linkedMapOf(
        "DC1" to Foo("10"),
        "DC2" to Foo("20"),
      )
    )
  }
})
