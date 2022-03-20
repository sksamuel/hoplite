package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.decoder.DotPath
import java.security.MessageDigest

/**
 * Implementations can choose how to obfuscate values.
 */
interface Obfuscator {
  fun obfuscate(path: DotPath, value: String): String
}

object StrictObfuscator : Obfuscator {
  override fun obfuscate(path: DotPath, value: String): String = "*****"
}

/**
 * Obfuscates values by taking the first 8 characters of the SHA-256 hash of the input value.
 */
object HashObfuscator : Obfuscator {
  override fun obfuscate(path: DotPath, value: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(value.encodeToByteArray()).decodeToString().take(8)
  }
}

/**
 * An [Obfuscator] that returns the first three characters only of every field.
 */
object DefaultObfuscator : Obfuscator {
  override fun obfuscate(path: DotPath, value: String): String = value.take(3) + "*****"
}
