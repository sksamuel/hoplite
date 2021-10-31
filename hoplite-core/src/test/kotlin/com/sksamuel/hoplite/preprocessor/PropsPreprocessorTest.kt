package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class PropsPreprocessorTest : StringSpec() {
  init {
    "should replace props from file" {

      data class Config(val a: String, val b: String)

      val preprocessor = PropsPreprocessor("/sample.properties")

      val config = ConfigLoader.Builder()
        .addPreprocessor(preprocessor)
        .build()
        .loadConfigOrThrow<Config>("/processme.props")

      config shouldBe Config(a = "I'm on branch master", b = "this replacement doesn't exist \${foo}")
    }

    "should replace props from file, by defaults before specified" {
      data class Config(val a: String, val b: String)

      withEnvironment(mapOf("git.branch" to "main")) {

        val preprocessor = PropsPreprocessor("/sample.properties")

        val config = ConfigLoader.Builder()
          .addDefaultPreprocessors()
          .addPreprocessor(preprocessor)
          .build()
          .loadConfigOrThrow<Config>("/processme.props")

        config shouldBe Config(a = "I'm on branch main", b = "this replacement doesn't exist \${foo}")
      }
    }

    "should replace props from file, by specified before defaults" {
      data class Config(val a: String, val b: String)

      withEnvironment(mapOf("git.branch" to "main", "foo" to "nevermind, it does!")) {
        val preprocessor = PropsPreprocessor("/sample.properties")

        val config = ConfigLoader.Builder()
          .addPreprocessor(preprocessor)
          .addDefaultPreprocessors()
          .build()
          .loadConfigOrThrow<Config>("/processme.props")

        config shouldBe Config(a = "I'm on branch master",
          b = "this replacement doesn't exist nevermind, it does!")
      }
    }
  }
}
