package com.sksamuel.hoplite.report

import java.security.MessageDigest

/**
 * Implementations can choose how to obfuscate values.
 */
interface Obfuscator {
  fun obfuscate(value: String): String
}

/**
 * An [Obfuscator] that returns a fixed mask.
 */
object StrictObfuscator : Obfuscator {
  override fun obfuscate(value: String): String = "*****"
}

/**
 * An [Obfuscator] that takes the first 8 characters of the SHA-256 hash of the input value.
 */
object HashObfuscator : Obfuscator {
  override fun obfuscate(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest
      .digest(value.encodeToByteArray())
      .take(8)
      .joinToString("", "hash(", "...)") { "%02x".format(it) }
  }
}


/**
 * An [Obfuscator] that returns the first three characters with a fixed mask.
 */
object DefaultObfuscator : Obfuscator {
  override fun obfuscate(value: String): String = value.take(3) + "*****"
}
