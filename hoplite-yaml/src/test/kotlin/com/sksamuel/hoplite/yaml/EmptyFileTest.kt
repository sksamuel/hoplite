package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EmptyFileTest : StringSpec({

  "empty files should be skipped when enabled in config builder" {
    data class Test(val foo: String)

    val config = ConfigLoaderBuilder.default()
      .allowEmptySources()
      .build()
      .loadConfigOrThrow<Test>("/foo.yml", "/empty.yml")

    config shouldBe Test("bar")
  }
})
