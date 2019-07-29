package com.sksamuel.hoplite.preprocessor

object SystemPropertyPreprocessor : Preprocessor {
  private val regex = "\\$\\{(.*?)}".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    System.getProperty(key, it.value)
  }
}
