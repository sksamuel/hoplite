package com.sksamuel.hoplite.transformer

import com.sksamuel.hoplite.*

/**
 * A [NodeTransformer] is a function that transforms a node into another node. Any type of node transformation can
 * be applied at configuration loading time.
 */
interface NodeTransformer {
  /** Used for one of path element transformations equivalent to the node transformation. */
  fun transformPathElement(element: String): String

  fun transform(node: Node, sealedTypeDiscriminatorField: String?): Node
}
