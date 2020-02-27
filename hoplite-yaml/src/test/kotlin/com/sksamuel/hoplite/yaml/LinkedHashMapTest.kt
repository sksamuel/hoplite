package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class LinkedHashMapTest : StringSpec({
  "LinkedHashMap decoded from yaml" {
    data class Test(val a: LinkedHashMap<String, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/linked_hash_map.yml")
    config shouldBe Test(linkedMapOf("x" to "y", "z" to "c", "e" to "f", "g" to "h", "u" to "i"))
  }
})
