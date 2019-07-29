package com.sksamuel.hoplite.preprocessor

import java.util.*

object UUIDPreprocessor : Preprocessor {
  private val regex = "\\\$uuid\\(\\)".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    UUID.randomUUID().toString()
  }
}
