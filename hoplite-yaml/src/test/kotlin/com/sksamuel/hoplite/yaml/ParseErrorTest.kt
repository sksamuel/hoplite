package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldInclude

class ParseErrorTest : FunSpec() {
  init {
    test("foo") {
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addSource(PropertySource.resource("/bad_indent.yml"))
          .build()
          .loadNodeOrThrow()
      }.message shouldInclude """while parsing a block mapping
     in 'reader', line 22, column 5"""
    }
  }
}
