package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import java.io.File

class FileSourceTest : FunSpec() {
  init {

    test("PropertySource.file should should not fail for optionals") {
      ConfigLoaderBuilder.default().addSource(PropertySource.file(File("wibble"), true)).build().loadNodeOrThrow()
    }

    test("PropertySource.file should should fail for required file that does not exist") {
      shouldThrowAny {
        ConfigLoaderBuilder.default().addSource(PropertySource.file(File("wibble"), false)).build().loadNodeOrThrow()
      }
    }
  }
}
