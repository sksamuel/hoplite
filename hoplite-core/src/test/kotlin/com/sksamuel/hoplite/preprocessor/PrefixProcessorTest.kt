package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private object Upcaser : PrefixProcessor("upcase:") {
  override fun processString(valueWithoutPrefix: String): String = valueWithoutPrefix.uppercase()
}

class PrefixProcessorTest : StringSpec() {
  init {
    "should process string nodes with a prefix" {

      data class Config(val a: String, val b: String)

      val config = ConfigLoaderBuilder.default()
        .addPreprocessor(Upcaser)
        .build()
        .loadConfigOrThrow<Config>("/prefixProcessor.props")

      config shouldBe Config(a = "This is the quiet part", b = "YOU KIDS GET OFF MY LAWN")
    }
  }
}
