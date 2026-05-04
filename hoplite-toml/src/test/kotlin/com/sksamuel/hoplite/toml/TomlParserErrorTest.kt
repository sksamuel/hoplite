package com.sksamuel.hoplite.toml

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class TomlParserErrorTest : FunSpec({

  test("parser surfaces errors from invalid TOML instead of returning a partial result") {
    // Reserved characters / unquoted spaces are invalid in TOML.
    val invalid = """
      key1 = "ok"
      not valid toml here
      key2 = 42
    """.trimIndent()

    val ex = shouldThrow<IllegalStateException> {
      TomlParser().load(invalid.byteInputStream(), "inline.toml")
    }
    ex.message shouldContain "Invalid TOML in inline.toml"
  }

  test("parser still accepts a syntactically-valid document") {
    val valid = """
      name = "hoplite"
      count = 7
    """.trimIndent()

    // Should not throw.
    TomlParser().load(valid.byteInputStream(), "inline.toml")
  }
})
