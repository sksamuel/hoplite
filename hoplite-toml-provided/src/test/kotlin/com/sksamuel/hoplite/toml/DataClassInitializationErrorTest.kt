package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.contain

// https://github.com/sksamuel/hoplite/issues/165
class DataClassInitializationErrorTest : FunSpec() {
  init {
    test("an error when instantiating a class should be propagated") {
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addSource(TomlPropertySource("a = 123123213123"))
          .build()
          .loadConfigOrThrow<Foo>()
      }.message should contain("boom")
    }
  }
}

data class Foo(val a: String) {
  init {
    error("boom")
  }
}
