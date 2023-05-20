package com.sksamuel.hoplite.secrets

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import java.security.MessageDigest

/**
 * Implementations can choose how to obfuscate [PrimitiveNode]s for display in reports.
 */
interface Obfuscator {
  fun obfuscate(node: PrimitiveNode): String
}

/**
 * An [Obfuscator] that returns a fixed mask for every node.
 */
class StrictObfuscator(private val mask: String = "*****") : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String = mask
}

private fun isNumericOrBoolean(value: String): Boolean {

  val maybeBoolean = value.toBooleanStrictOrNull()
  if (maybeBoolean != null) return true

  val maybeLong = value.toLongOrNull()
  if (maybeLong != null) return true

  val maybeDouble = value.toDoubleOrNull()
  if (maybeDouble != null) return true

  return false
}

/**
 * An [Obfuscator] that takes the first n characters of the SHA-256 hash of the input value for strings,
 * and reports the values as is otherwise.
 */
class HashObfuscator(private val hashCharsToShow: Int = 8) : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> {
        if (isNumericOrBoolean(node.value)) return node.value
        val digest = MessageDigest.getInstance("SHA-256")
        return digest
          .digest(node.value.encodeToByteArray())
          .take(hashCharsToShow)
          .joinToString("", "hash(", "...)") { "%02x".format(it) }
      }
    }
  }
}

/**
 * An [Obfuscator] that returns the first [prefixLength] characters along with
 * a [suffixMask] for Strings, for other values returns the full value.
 */
class PrefixObfuscator(
  private val prefixLength: Int,
  private val suffixMask: String = "*****"
) : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> {
        return when {
          isNumericOrBoolean(node.value) -> node.value
          node.value.length <= prefixLength -> node.value
          else -> node.value.take(prefixLength) + suffixMask
        }
      }
    }
  }
}
