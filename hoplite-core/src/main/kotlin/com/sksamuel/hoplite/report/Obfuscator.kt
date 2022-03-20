package com.sksamuel.hoplite.report

/**
 * Implementations can choose how to obfuscate values.
 */
interface Obfuscator {
  fun obfuscate(value: String): String
}

/**
 * Obfuscates by returning the first 2 characters only.
 */
object DefaultObfuscator : Obfuscator {
  override fun obfuscate(value: String): String = value.take(2) + "*****"
}
