package com.sksamuel.hoplite.transformer

import com.sksamuel.hoplite.*

/**
 * A [NodeTransformer] is a function that transforms a node into another node. Any type of node transformation can
 * be applied at configuration loading time.
 */
interface NodeTransformer {
  fun transform(node: Node, sealedTypeDiscriminatorField: String?): Node
}
