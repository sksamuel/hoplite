package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.Preprocessor

/**
 * Applies the pre-processing steps to a [Node] tree.
 */
class Preprocessing(
  private val preprocessors: List<Preprocessor>,
  private val iterations: Int,
) {

  fun preprocess(node: Node, context: DecoderContext): ConfigResult<Node> {
    return iterate(node, iterations, context)
  }

  private fun iterate(node: Node, iterations: Int, context: DecoderContext): Validated<ConfigFailure, Node> =
    if (iterations == 0) node.valid() else process(node, context).flatMap { iterate(it, iterations - 1, context) }

  private fun process(node: Node, context: DecoderContext): Validated<ConfigFailure, Node> =
    preprocessors.fold<Preprocessor, ConfigResult<Node>>(node.valid()) { acc, preprocessor ->
      acc.flatMap { preprocessor.process(it, context) }
    }
}
