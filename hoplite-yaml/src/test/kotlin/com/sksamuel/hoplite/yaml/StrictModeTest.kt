package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class StrictModeTest : FunSpec() {
  init {

    test("strict mode should error when unused config remains") {

      data class Foo(
        val tipTap: Boolean,
        val wibbleWobble: Double
      )

      shouldThrowAny {
        ConfigLoaderBuilder
          .default()
          .strict()
          .build()
          .loadConfigOrThrow<Foo>("/snake_case.yml")
      }.message
        .shouldContain("dripDrop at (/snake_case.yml:0:10) was unused")
        .shouldContain("double_trouble at (/snake_case.yml:2:16) was unused")
    }

    test("strict mode should not error when all config is used") {

      data class Foo(
        val dripDrop: String,
        val tipTap: Boolean,
        val doubleTrouble: List<String>,
        val wibbleWobble: Double
      )

      ConfigLoaderBuilder
        .default()
        .strict()
        .build()
        .loadConfigOrThrow<Foo>("/snake_case.yml")
    }

    test("strict mode should error when nested unused config remains") {

      data class Bar(
        val x: String,
        val e: String,
      )

      data class Foo(
        val A: Bar,
      )

      shouldThrowAny {
        ConfigLoaderBuilder
          .default()
          .strict()
          .build()
          .loadConfigOrThrow<Foo>("/linked_hash_map.yml")
      }.message
        .shouldContain("a.z at (/linked_hash_map.yml:2:5) was unused")
        .shouldContain("a.g at (/linked_hash_map.yml:4:5) was unused")
        .shouldContain("a.u at (/linked_hash_map.yml:5:5) was unused")
        .shouldNotContain("a.x at (/linked_hash_map.yml:2:5) was unused")
        .shouldNotContain("a.e at (/linked_hash_map.yml:2:5) was unused")
    }
  }
}
