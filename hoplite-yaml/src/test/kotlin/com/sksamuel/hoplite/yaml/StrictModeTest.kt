package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class StrictModeTest : FunSpec() {
  init {
    val basicStrictConfigLoaderBuilder = ConfigLoaderBuilder.defaultWithoutPropertySources().strict()

    test("strict mode should error when unused config remains") {

      data class Foo(
        val tipTap: Boolean,
        val wibbleWobble: Double
      )

      shouldThrowAny {
        basicStrictConfigLoaderBuilder.build()
          .loadConfigOrThrow<Foo>("/snake_case.yml")
      }.message
        .shouldContain("Config value 'dripDrop' at (classpath:/snake_case.yml:0:10) was unused")
        .shouldContain("Config value 'double_trouble' at (classpath:/snake_case.yml:2:16) was unused")
    }

    test("strict mode should not error when all config is used") {

      data class Foo(
        val dripDrop: String,
        val tipTap: Boolean,
        val doubleTrouble: List<String>,
        val wibbleWobble: Double
      )

      basicStrictConfigLoaderBuilder.build()
        .loadConfigOrThrow<Foo>("/snake_case.yml")
    }

    test("strict mode should error when nested unused config remains") {

      data class Bar(
        val x: String,
        val e: String
      )

      data class Foo(
        val A: Bar
      )

      shouldThrowAny {
        basicStrictConfigLoaderBuilder.build()
          .loadConfigOrThrow<Foo>("/linked_hash_map.yml")
      }.message
        .shouldContain("Config value 'a.z' at (classpath:/linked_hash_map.yml:2:5) was unused")
        .shouldContain("Config value 'a.g' at (classpath:/linked_hash_map.yml:4:5) was unused")
        .shouldContain("Config value 'a.u' at (classpath:/linked_hash_map.yml:5:5) was unused")
        .shouldNotContain("Config value 'a.x' at (classpath:/linked_hash_map.yml:2:5) was unused")
        .shouldNotContain("Config value 'a.e' at (classpath:/linked_hash_map.yml:2:5) was unused")
    }

    test("strict mode should take into account param mappers") {
      data class Foo(val bubbleBobble: String)
      ConfigLoaderBuilder
        .defaultWithoutPropertySources()
        .strict()
        .addSource(YamlPropertySource("bubble_bobble: xyz"))
        .addSource(YamlPropertySource("bubbleBobble: xyz"))
        .build()
        .loadConfigOrThrow<Foo>()
        .bubbleBobble shouldBe "xyz"
    }

    test("strict mode should not error when using maps") {
      data class NumberDescription(
        val number: Int
      )
      data class NumberMapContainer(
        val counting: Map<String, NumberDescription>
      )

      basicStrictConfigLoaderBuilder.build()
        .loadConfigOrThrow<NumberMapContainer>("/numbers_map.yml")
    }
  }
}
