package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.yaml.com.sksamuel.hoplite.yaml.Yaml
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDate

class NestedClassTest : FunSpec({

  test("support two-level nested data classes") {
    data class Inner(val a: String, val b: String)
    data class Outer(val a: String, val b: Inner, val c: String)
    ConfigLoader(Yaml).loadConfig<Outer>("/test_nested2.yml").shouldBeValid {
      it.a shouldBe Outer("foo", Inner("goo", "moo"), "voo")
    }
  }

  test("support three-level nested data classes") {
    data class Inner(val k: Int, val j: LocalDate)
    data class Middle(val a: Inner, val b: Double, val c: Long)
    data class Outer(val x: String, val y: Boolean, val z: Middle)
    ConfigLoader(Yaml).loadConfig<Outer>("/test_nested3.yml").shouldBeValid {
      it.a shouldBe Outer("foo", true, Middle(Inner(12, LocalDate.of(2016, 5, 12)), 7924.3, 91894781923))
    }
  }
})