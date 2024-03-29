package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class PropsPreprocessorTest : StringSpec() {
  init {
    "should replace props from file" {

      data class Config(val a: String)

      val preprocessor = PropsPreprocessor("/sample.properties")

      val config = ConfigLoaderBuilder.default()
        .addPreprocessor(preprocessor)
        .build()
        .loadConfigOrThrow<Config>("/processme.props")

      config shouldBe Config(a = "I'm on branch master")
    }

    "should replace props from file, by defaults before specified" {
      data class Config(val a: String)

      withEnvironment(mapOf("git.branch" to "main")) {

        val preprocessor = PropsPreprocessor("/sample.properties")

        val config = ConfigLoaderBuilder.default()
          .addDefaultPreprocessors()
          .addPreprocessor(preprocessor)
          .build()
          .loadConfigOrThrow<Config>("/processme.props")

        config shouldBe Config(a = "I'm on branch main")
      }
    }

    "should replace props from file, by specified before defaults" {
      data class Config(val a: String)

      withEnvironment(mapOf("git.branch" to "main")) {
        val preprocessor = PropsPreprocessor("/sample.properties")

        val config = ConfigLoaderBuilder.empty()
          .addPreprocessor(preprocessor)
          .addDefaults()
          .build()
          .loadConfigOrThrow<Config>("/processme.props")

        config shouldBe Config(a = "I'm on branch master")
      }
    }
  }
}
