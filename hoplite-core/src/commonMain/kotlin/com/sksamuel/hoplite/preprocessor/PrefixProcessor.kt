package com.sksamuel.hoplite.preprocessor

/**
 * Process property strings that start with a given prefix. When specifying the prefix, include any punctuation
 * separating it from the actual value. The entire prefix, including any punctuation, will be stripped off before
 * the value is sent to the [processString] method.
 */
abstract class PrefixProcessor(private val prefix: String) : TraversingPrimitivePreprocessor() {

  abstract fun processString(valueWithoutPrefix: String): String

  override fun handle(node: PrimitiveNode): Node {
    return if (node is StringNode && node.value.startsWith(prefix)) {
      node.copy(value = processString(node.value.substring(prefix.length)))
    } else {
      node
    }
  }
}
