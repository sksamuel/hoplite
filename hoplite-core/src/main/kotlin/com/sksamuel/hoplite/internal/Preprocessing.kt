package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import java.util.concurrent.ConcurrentHashMap

/**
 * Applies the pre-processing steps to a [Node] tree.
 *
 * With [cacheValueResults], each preprocessor that works on one value at a time keeps its result for each value node
 * (same value, path and source) and gives it again whenever that node is preprocessed, so it runs once per value.
 * With lookup sources the config tree is built and preprocessed for each bind (gh-606), so without this a
 * [com.sksamuel.hoplite.ConfigBinder] with several binds would fetch each secret (secret manager and vault
 * preprocessors) several times, give different random values to different binds, and report each lookup several
 * times. Preprocessors that look at the whole tree run on every preprocess.
 */
class Preprocessing(
  preprocessors: List<Preprocessor>,
  private val iterations: Int,
  cacheValueResults: Boolean = false,
) {

  private val preprocessors = if (cacheValueResults) preprocessors.map { it.withValueCache() } else preprocessors

  fun preprocess(node: Node, context: DecoderContext): ConfigResult<Node> {
    return iterate(node, iterations, context)
  }

  private fun iterate(node: Node, iterations: Int, context: DecoderContext): Validated<ConfigFailure, Node> =
    if (iterations == 0) node.valid() else process(node, context).flatMap { iterate(it, iterations - 1, context) }

  private fun process(node: Node, context: DecoderContext): Validated<ConfigFailure, Node> =
    preprocessors.fold<Preprocessor, ConfigResult<Node>>(node.valid()) { acc, preprocessor ->
      acc.flatMap { preprocessor.process(it, context) }
    }

  /**
   * Returns this preprocessor with a cache of its result per value, if it works on one value at a time: a
   * [TraversingPrimitivePreprocessor] that doesn't change how it walks the tree. Otherwise returns it as is.
   */
  @Suppress("DEPRECATION")
  private fun Preprocessor.withValueCache(): Preprocessor {
    if (this !is TraversingPrimitivePreprocessor) return this
    val process = javaClass.getMethod("process", Node::class.java, DecoderContext::class.java)
    if (process.declaringClass != TraversingPrimitivePreprocessor::class.java) return this
    val delegate = this
    val results = ConcurrentHashMap<PrimitiveNode, ConfigResult<Node>>()
    return object : TraversingPrimitivePreprocessor() {
      override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> =
        results.computeIfAbsent(node) { delegate.handle(node, context) }
    }
  }
}
