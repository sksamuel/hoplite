package com.sksamuel.hoplite.preprocessor

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
