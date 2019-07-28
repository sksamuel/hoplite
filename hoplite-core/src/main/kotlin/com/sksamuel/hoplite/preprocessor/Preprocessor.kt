package com.sksamuel.hoplite.preprocessor

import kotlin.random.Random

/**
 * Takes a raw config value and processes it.
 */
interface Preprocessor {
  fun process(value: String): String
}

fun defaultPreprocessors() = listOf(EnvVarPreprocessor, SystemPropertyPreprocessor, RandomPreprocessor)

object EnvVarPreprocessor : Preprocessor {
  private val regex = "\\$\\{(.*?)}".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    System.getenv(key) ?: it.value
  }
}

object SystemPropertyPreprocessor : Preprocessor {
  private val regex = "\\$\\{(.*?)}".toRegex()
  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    System.getProperty(key, it.value)
  }
}

abstract class PrefixProcessor(private val prefix: String) : Preprocessor {
  abstract fun handle(value: String): String
  override fun process(value: String): String =
      if (value.startsWith(prefix)) handle(value) else value
}

object RandomPreprocessor : Preprocessor {
  private val regex = "\\\$RANDOM_STRING\\((\\d+)\\)".toRegex()
  private const val a = 33 // '!'
  private val z = 126 // '~'
  override fun process(value: String): String = regex.replace(value) {
    val length = it.groupValues[1].toInt()
    val chars = CharArray(length) { Random.nextInt(33, 126).toChar() }
    String(chars)
  }
}
