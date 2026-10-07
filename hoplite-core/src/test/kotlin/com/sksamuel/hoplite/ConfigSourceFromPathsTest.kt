package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

class ConfigSourceFromPathsTest : FunSpec({

  test("fromPaths returns Valid for existing paths") {
    val tempFile = Files.createTempFile("hoplite-cs-test", ".yml")
    try {
      Files.writeString(tempFile, "x: 1")
      val result = ConfigSource.fromPaths(listOf(tempFile))
      result.shouldBeInstanceOf<Validated.Valid<List<ConfigSource>>>()
      result.value.size shouldBe 1
    } finally {
      tempFile.deleteIfExists()
    }
  }

  test("fromPaths returns Invalid for missing paths") {
    val missing = Paths.get("/nonexistent/hoplite-cs-test-${System.nanoTime()}.yml")
    val result = ConfigSource.fromPaths(listOf(missing))
    result.shouldBeInstanceOf<Validated.Invalid<*>>()
  }

  test("fromClasspathResources returns Valid for existing resources") {
    // basic.props is a fixture under hoplite-core test resources
    val result = ConfigSource.fromClasspathResources(listOf("/basic.props"))
    result.shouldBeInstanceOf<Validated.Valid<List<ConfigSource>>>()
    result.value.size shouldBe 1
  }

  test("fromClasspathResources returns Invalid for missing resources") {
    val result = ConfigSource.fromClasspathResources(listOf("/no-such-resource-${System.nanoTime()}.yml"))
    result.shouldBeInstanceOf<Validated.Invalid<*>>()
  }
})
