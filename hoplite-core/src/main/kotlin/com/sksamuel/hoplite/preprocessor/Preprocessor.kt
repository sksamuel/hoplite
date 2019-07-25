package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value

/**
 * Takes a raw config value and processes it.
 */
interface Preprocessor {
  fun process(value: Value): Value
}

object EnvVarPreprocessor : Preprocessor {

  private val regex = "\\$\\{(.*?)}".toRegex()

  override fun process(value: Value): Value = when (value) {
    is StringValue -> StringValue(regex.replace(value.value) { it.groupValues[1] }, value.pos)
    else -> value
  }
}

abstract class PrefixProcessor(private val prefix: String) : Preprocessor {
  abstract fun handle(value: Value): Value
  override fun process(value: Value): Value = when (value) {
    is StringValue -> if (value.value.startsWith(prefix)) handle(value) else value
    else -> value
  }
}