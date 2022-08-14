package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import java.security.MessageDigest

/**
 * Implementations can choose how to obfuscate values.
 */
interface Obfuscator {
  fun obfuscate(node: PrimitiveNode): String
}

/**
 * An [Obfuscator] that returns a fixed mask for every node.
 */
object StrictObfuscator : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String = "*****"
}

/**
 * An [Obfuscator] that takes the first 8 characters of the SHA-256 hash of the input value for strings,
 * and reports the values as is otherwise.
 */
object HashObfuscator : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> {

        val maybeBoolean = node.value.toBooleanStrictOrNull()
        if (maybeBoolean != null) return maybeBoolean.toString()

        // longs must be first, otherwise they will be changed to a double
        val maybeLong = node.value.toLongOrNull()
        if (maybeLong != null) return maybeLong.toString()

        val maybeDouble = node.value.toDoubleOrNull()
        if (maybeDouble != null) return maybeDouble.toString()

        val digest = MessageDigest.getInstance("SHA-256")
        return digest
          .digest(node.value.encodeToByteArray())
          .take(8)
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
  private val suffixMask: String = "*****",
) : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> {
        val maybeBoolean = node.value.toBooleanStrictOrNull()
        if (maybeBoolean != null) return maybeBoolean.toString()

        // longs must be first, otherwise they will be changed to a double
        val maybeLong = node.value.toLongOrNull()
        if (maybeLong != null) return maybeLong.toString()

        val maybeDouble = node.value.toDoubleOrNull()
        if (maybeDouble != null) return maybeDouble.toString()

        return node.value.take(prefixLength) + suffixMask
      }
    }
  }
}

/**
 * An [Obfuscator] that returns the first three characters with a fixed mask for strings,
 * and the value otherwise.
 */
@Deprecated("Use PrefixObfuscator")
object DefaultObfuscator : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> {
        val maybeBoolean = node.value.toBooleanStrictOrNull()
        if (maybeBoolean != null) return maybeBoolean.toString()

        // longs must be first, otherwise they will be changed to a double
        val maybeLong = node.value.toLongOrNull()
        if (maybeLong != null) return maybeLong.toString()

        val maybeDouble = node.value.toDoubleOrNull()
        if (maybeDouble != null) return maybeDouble.toString()

        return node.value.take(3) + "*****"
      }
    }
  }
}
