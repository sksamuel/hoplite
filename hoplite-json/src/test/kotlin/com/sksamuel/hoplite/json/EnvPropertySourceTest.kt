package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addEnvironmentSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvPropertySourceTest : FunSpec({

  test("loading from envs") {
    data class TestConfig(val foo: String, val woo: String)
    withEnvironment(mapOf("foo" to "a", "woo" to "b")) {
      ConfigLoader.builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<TestConfig>() shouldBe TestConfig("a", "b")
    }
  }
  test("envs should override local file") {
    data class TestConfig(val foo: String, val woo: String)
    withEnvironment(mapOf("foo" to "a")) {
      ConfigLoader.builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<TestConfig>("/sysproptest1.props") shouldBe TestConfig("a", "y")
    }
  }
  test("nested envs should overwrite value") {
    data class Bar(val s: Long, val t: Long)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    withEnvironment(mapOf("foo__bar__s" to "1")) {
      ConfigLoader.builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar(1, 6)))
    }
  }
  test("parent envs should be ignored if not mapped to config") {
    data class Bar(val s: String, val t: String)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    // the sys prop foo is a parent of our bar.s but since Foo maps to a data class, the prop should never be used
    withEnvironment(mapOf("foo__bar__s" to "x", "foo" to "y")) {
      ConfigLoader.builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar("x", "6")))
    }
  }
  test("child envs should be ignored if not mapped to config") {
    data class Bar(val s: String, val t: String)
    data class Foo(val bar: Bar)
    data class TestConfig(val foo: Foo)
    // the sysprop foo.bar.s has a child foo.bar.s.u which is not required, but the parent value should still be used
    withEnvironment(mapOf("foo__bar__s" to "x", "foo__bar__s__u" to "y")) {
      ConfigLoader.builder()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<TestConfig>("/sysproptest2.json") shouldBe TestConfig(Foo(Bar("x", "6")))
    }
  }
})
