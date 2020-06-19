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
    is MapNode -> MapNode(node.map.map { (k, v) -> k to process(v) }.toMap(), node.pos)
    is ArrayNode -> ArrayNode(node.elements.map { process(node) }, node.pos)
    is PrimitiveNode -> handle(node)
    else -> node
  }
}

fun defaultPreprocessors() = listOf(
  EnvVarPreprocessor,
  SystemPropertyPreprocessor,
  RandomPreprocessor
)

abstract class PrefixProcessor(private val prefix: String) : Preprocessor {

  abstract fun handle(node: StringNode): Node

  override fun process(node: Node): Node = when (node) {
    is StringNode -> if (node.value.startsWith(prefix)) handle(node) else node
    else -> node
  }
}

