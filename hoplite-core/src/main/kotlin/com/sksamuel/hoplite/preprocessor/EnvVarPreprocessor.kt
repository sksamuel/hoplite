package com.sksamuel.hoplite.preprocessor

object EnvVarPreprocessor : Preprocessor {
  private val regex = "\\$\\{(.*?)}".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    System.getenv(key) ?: it.value
  }
}
