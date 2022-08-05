package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SnakeCaseTest : StringSpec({

  "testing config with a mix of snake and camel case" {
    data class Foo(
      val dripDrop: String,
      val tipTap: Boolean,
      val doubleTrouble: List<String>,
      val wibbleWobble: Double
    )
    ConfigLoader().loadConfigOrThrow<Foo>("/snake_case.yml") shouldBe
      Foo(dripDrop = "hello", tipTap = true, doubleTrouble = listOf("1", "2", "3"), wibbleWobble = 124.55)
  }

  "should support trailing numbers as components" {
    data class Foo(
      val myServer1: String,
      val myServer2: String,
      val myServer11: String,
      val myServer12: String,
    )
    ConfigLoader.builder()
      .addSource(YamlPropertySource("my_server_1: a"))
      .addSource(YamlPropertySource("my_server2: b"))
      .addSource(YamlPropertySource("my_server_11: c"))
      .addSource(YamlPropertySource("my_server12: d"))
      .build()
      .loadConfigOrThrow<Foo>() shouldBe Foo(myServer1 = "a", myServer2 = "b", myServer11 = "c", myServer12 = "d")
  }
})
