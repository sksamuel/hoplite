package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDate

class NestedClassTest : FunSpec({

  test("support two-level nested data classes") {
    data class Inner(val a: String, val b: String)
    data class Outer(val a: String, val b: Inner, val c: String)

    val config = ConfigLoader().loadConfigOrThrow<Outer>("/test_nested2.yml")
    config shouldBe Outer("foo", Inner("goo", "moo"), "voo")
  }

  test("support three-level nested data classes") {
    data class Inner(val k: Int, val j: LocalDate)
    data class Middle(val a: Inner, val b: Double, val c: Long)
    data class Outer(val x: String, val y: Boolean, val z: Middle)

    val config = ConfigLoader().loadConfigOrThrow<Outer>("/test_nested3.yml")
    config shouldBe Outer("foo", true, Middle(Inner(12, LocalDate.of(2016, 5, 12)), 7924.3, 91894781923))
  }
})
