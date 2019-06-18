package com.sksamuel.hoplite

/**
 * Takes a raw config value and processes it.
 */
interface Preprocessor {
  fun process(value: String): String
}

object EnvVarPreprocessor : Preprocessor {

  private val regex = "\\$\\{(.*?)}".toRegex()

  override fun process(value: String): String {
    return regex.replace(value) { it.groupValues[1] }
  }
}

