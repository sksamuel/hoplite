package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.extensions.system.withSystemProperties
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class SystemPropertySourceTest : FunSpec({

  test("loading from sys props") {
    data class TestConfig(val foo: String, val woo: String)
    withSystemProperties(mapOf("config.override.foo" to "a", "config.override.woo" to "b")) {
      ConfigLoader().loadConfigOrThrow<TestConfig>() shouldBe TestConfig("a", "b")
    }
  }
  test("sys prop should override local file") {
    data class TestConfig(val foo: String, val woo: String)
    withSystemProperties(mapOf("config.override.foo" to "a")) {
      ConfigLoader().loadConfigOrThrow<TestConfig>("/sysproptest1.props") shouldBe TestConfig("a", "y")
    }
  }
  test("nested sys prop should overwrite value") {
    data class Bar(val s: Long, val t: Long)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    withSystemProperties(mapOf("config.override.foo.bar.s" to "1")) {
      ConfigLoader().loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar(1, 6)))
    }
  }
  test("parent sys props should be ignored if not mapped to config") {
    data class Bar(val s: String, val t: String)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    // the sys prop foo is a parent of our bar.s but since Foo maps to a data class, the prop should never be used
    withSystemProperties(mapOf("config.override.foo.bar.s" to "x", "config.override.foo" to "y")) {
      ConfigLoader().loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar("x", "6")))
    }
  }
  test("child sys props should be ignored if not mapped to config") {
    data class Bar(val s: String, val t: String)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    // the sysprop foo.bar.s has a child foo.bar.s.u which is not required, but the parent value should still be used
    withSystemProperties(mapOf("config.override.foo.bar.s" to "x", "config.override.foo.bar.s.u" to "y")) {
      ConfigLoader().loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar("x", "6")))
    }
  }
})
