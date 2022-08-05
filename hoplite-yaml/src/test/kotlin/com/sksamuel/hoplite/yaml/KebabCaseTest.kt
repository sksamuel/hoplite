package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KebabCaseTest : StringSpec({

  "should support trailing numbers as components" {
    data class Foo(
      val myServer1: String,
      val myServer2: String,
      val myServer11: String,
      val myServer12: String,
    )
    ConfigLoader.builder()
      .addSource(YamlPropertySource("my-server-1: a"))
      .addSource(YamlPropertySource("my-server2: b"))
      .addSource(YamlPropertySource("my-server-11: c"))
      .addSource(YamlPropertySource("my-server12: d"))
      .build()
      .loadConfigOrThrow<Foo>() shouldBe Foo(myServer1 = "a", myServer2 = "b", myServer11 = "c", myServer12 = "d")
  }
})
