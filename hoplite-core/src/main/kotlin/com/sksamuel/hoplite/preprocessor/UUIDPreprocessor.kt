package com.sksamuel.hoplite.preprocessor

import java.util.*

@Deprecated("replaced with \${random.uuid}")
object UUIDPreprocessor : Preprocessor {
  private val regex = "\\\$uuid\\(\\)".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    UUID.randomUUID().toString()
  }
}
