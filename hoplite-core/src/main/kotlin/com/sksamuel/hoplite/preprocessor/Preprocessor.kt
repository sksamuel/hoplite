package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode

/**
 * A [Preprocessor] applies a function to a root [Node] before that root node is
 * used to decode values.
 *
 * It is the responsibility of the preprecessors to traverse the node tree applying
 * to child nodes if applicable.
 *
 * A [TraversingPrimitivePreprocessor] is available to subclass
 * which will perform the task of descending into child nodes when a container node
 * is encountered.
 */
interface Preprocessor {
  fun process(node: Node): Node
}

/**
 * A preprocessor that will traverse the child nodes of maps and arrays, invoking [handle]
 * for any primitive nodes encountered.
 */
abstract class TraversingPrimitivePreprocessor : Preprocessor {

  abstract fun handle(node: PrimitiveNode): Node

  override fun process(node: Node): Node = when (node) {
    is MapNode -> {
      val value = if (node.value is PrimitiveNode) handle(node.value) else node.value
      MapNode(node.map.map { (k, v) -> k to process(v) }.toMap(), node.pos, value)
    }
    is ArrayNode -> ArrayNode(node.elements.map { process(it) }, node.pos)
    is PrimitiveNode -> handle(node)
    else -> node
  }
}

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

