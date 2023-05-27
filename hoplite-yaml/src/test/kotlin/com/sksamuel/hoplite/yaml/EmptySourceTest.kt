package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addPathSource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class EmptySourceTest : StringSpec({

  "empty files should be skipped when enabled in config builder" {
    data class Test(val foo: String)

    val config = ConfigLoaderBuilder.default()
      .allowEmptyConfigFiles()
      .build()
      .loadConfigOrThrow<Test>("/foo.yml", "/empty.yml")

    config shouldBe Test("bar")
  }

  "path source should support allowEmptyConfigFiles" {
    data class Test(val foo: String)

    val config = ConfigLoaderBuilder.default()
      .allowEmptyConfigFiles()
      .addPathSource(Paths.get(javaClass.getResource("/foo.yml").path))
      .addPathSource(Paths.get(javaClass.getResource("/empty.yml").path))
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test("bar")
  }

  "empty files should support files with only comments" {
    data class Test(val foo: String)

    val config = ConfigLoaderBuilder.default()
      .build()
      .loadConfigOrThrow<Test>("/foo.yml", "/empty_with_comments.yml")

    config shouldBe Test("bar")
  }
})
