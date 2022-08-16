package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalHoplite::class)
class StringSquashTest : FunSpec() {
  init {
    test("should squash arrays to a string when enabled") {
      data class Config(val a: String)
      ConfigLoaderBuilder.default()
        .flattenArraysToString()
        .build()
        .loadConfigOrThrow<Config>("/array.yml")
        .a shouldBe "1,2,3,4,3,2,1"
    }

    test("should NOT squash arrays to a string when not enabled") {
      data class Config(val a: String)
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .build()
          .loadConfigOrThrow<Config>("/array.yml")
      }.message shouldContain "Required type String could not be decoded from a List"
    }
  }
}
