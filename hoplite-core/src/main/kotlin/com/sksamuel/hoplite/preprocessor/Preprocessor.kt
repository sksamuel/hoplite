package com.sksamuel.hoplite.preprocessor

/**
 * Takes a raw config value and processes it.
 */
interface Preprocessor {
  fun process(value: String): String
}

fun defaultPreprocessors() = listOf(EnvVarPreprocessor,
  SystemPropertyPreprocessor,
  RandomPreprocessor,
  UUIDPreprocessor)

abstract class PrefixProcessor(private val prefix: String) : Preprocessor {
  abstract fun handle(value: String): String
  override fun process(value: String): String =
      if (value.startsWith(prefix)) handle(value) else value
}

