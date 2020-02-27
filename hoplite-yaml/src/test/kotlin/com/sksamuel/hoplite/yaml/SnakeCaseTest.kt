package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class SnakeCaseTest : StringSpec({
  "testing config with a mix of snake and camel case"{
    data class Foo(val dripDrop: String,
                   val tipTap: Boolean,
                   val doubleTrouble: List<String>,
                   val wibbleWobble: Double)
    ConfigLoader().loadConfigOrThrow<Foo>("/snake_case.yml") shouldBe
      Foo(dripDrop = "hello", tipTap = true, doubleTrouble = listOf("1", "2", "3"), wibbleWobble = 124.55)
  }
})
