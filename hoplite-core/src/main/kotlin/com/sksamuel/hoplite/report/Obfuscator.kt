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
 * An [Obfuscator] that returns the first three characters with a fixed mask for strings,
 * and the value otherwise.
 */
object DefaultObfuscator : Obfuscator {
  override fun obfuscate(node: PrimitiveNode): String {
    return when (node) {
      is BooleanNode -> node.value.toString()
      is NullNode -> node.value.toString()
      is DoubleNode -> node.value.toString()
      is LongNode -> node.value.toString()
      is StringNode -> node.value.take(3) + "*****"
    }
  }
}
