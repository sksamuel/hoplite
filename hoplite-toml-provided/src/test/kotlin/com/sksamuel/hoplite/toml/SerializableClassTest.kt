package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

data class NestedSerializableConfig(val a: String, val b: Boolean)

data class SerializableConfig(val l: Long, val d: Double, val nested: NestedSerializableConfig)

class SerializableClassTest : WordSpec() {
  init {
    "A class marked with @Serializable" should {
      "be usable as a config class" {
        ConfigLoader().loadConfigOrThrow<SerializableConfig>("/serializable.toml") shouldBe
          SerializableConfig(l = 123, d = 451.23, nested = NestedSerializableConfig(a = "foo", b = true))
      }
    }
  }
}
