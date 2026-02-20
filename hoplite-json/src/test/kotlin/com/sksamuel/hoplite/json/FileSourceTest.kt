package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import java.io.File

class FileSourceTest : FunSpec() {
  init {

    test("PropertySource.file should not fail for optionals") {
      ConfigLoaderBuilder.default()
        .addSource(PropertySource.file(File("foo.json"), true)).build().loadNodeOrThrow()
    }

    test("PropertySource.file should fail for required file that does not exist") {
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addSource(PropertySource.file(File("foo.json")))
          .build().loadNodeOrThrow()
      }.message shouldContain "Could not find file foo.json"
    }

    test("PropertySource.file should fail for empty file") {
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addSource(PropertySource.file(File(this::class.java.getResource("/empty.json")!!.file)))
          .build().loadNodeOrThrow()
      }.message shouldContain "empty.json is empty"
    }

    test("PropertySource.file should NOT fail for empty file if allowEmpty=true") {
      ConfigLoaderBuilder.default()
        .addSource(PropertySource.file(File(this::class.java.getResource("/empty.json")!!.file), allowEmpty = true))
        .build().loadNodeOrThrow()
    }

    test("PropertySource.file should fail if extension is not registered") {
      shouldThrowAny {
        ConfigLoaderBuilder.default()
          .addSource(PropertySource.file(File("wibble")))
          .build().loadNodeOrThrow()
      }.message shouldContain "Could not detect parser for file extension '.wibble'"
    }
  }
}
