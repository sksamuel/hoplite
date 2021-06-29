package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

data class Config(
  val sealed: Sealed
)

sealed class Sealed
data class One(val a: String) : Sealed()
data class Two(val a: String, val b: Int) : Sealed()

class OverrideTest : FunSpec() {
  init {
    // see https://github.com/sksamuel/hoplite/issues/98
    test("sealed classes should detect type based on full parameters") {
      ConfigLoader.Builder()
        .addSource(PropertySource.resource("/override1.toml"))
        .addSource(PropertySource.resource("/override2.toml"))
        .build()
        .loadConfigOrThrow<Config>().sealed shouldBe One("foo")

      ConfigLoader.Builder()
        .addSource(PropertySource.resource("/override2.toml"))
        .addSource(PropertySource.resource("/override1.toml"))
        .build()
        .loadConfigOrThrow<Config>().sealed shouldBe Two("bar", 1)
    }
  }
}
