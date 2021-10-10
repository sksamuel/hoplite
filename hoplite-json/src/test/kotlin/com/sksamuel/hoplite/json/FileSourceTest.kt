package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import io.kotest.core.spec.style.FunSpec
import java.io.File

class FileSourceTest : FunSpec() {
  init {
    test("PropertySource.file should should not fail for optionals") {
      ConfigLoader.Builder().addSource(PropertySource.file(File("wibble"), true)).build().loadNodeOrThrow()
    }
  }
}
