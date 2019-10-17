package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class LinkedHashMapTest : StringSpec({
  "LinkedHashMap decoded from yaml" {
    data class Test(val a: LinkedHashMap<String, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/linked_hash_map.yml")
    config shouldBe Test(linkedMapOf("x" to "y", "z" to "c", "e" to "f", "g" to "h", "u" to "i"))
  }
})
