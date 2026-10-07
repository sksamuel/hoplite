package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addCommandLineSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CommandLinePropertySourceTest : FunSpec() {
  init {

    test("should parse prefixed key=value arguments") {
      data class Config(val foo: String, val bar: String)

      val config = ConfigLoader.builder()
        .addCommandLineSource(arrayOf("--foo=hello", "--bar=world", "ignored=nope"))
        .build()
        .loadConfigOrThrow<Config>()

      config shouldBe Config("hello", "world")
    }

    // When the delimiter is a substring of the prefix (e.g. prefix "--", delimiter "-"),
    // a flag-style argument with no delimiter in its key portion (e.g. "--verbose") used to
    // crash with IndexOutOfBoundsException: the "contains(delimiter)" guard was applied to the
    // raw argument (which contains the delimiter inside the prefix) rather than to the
    // post-prefix key, so the split produced no value and element[1] was accessed.
    test("should skip prefixed arguments with no delimiter in the key portion") {
      data class Config(val foo: String)

      val config = ConfigLoader.builder()
        .addCommandLineSource(arrayOf("--verbose", "--foo-bar"), prefix = "--", delimiter = "-")
        .build()
        .loadConfigOrThrow<Config>()

      config shouldBe Config("bar")
    }
  }
}
