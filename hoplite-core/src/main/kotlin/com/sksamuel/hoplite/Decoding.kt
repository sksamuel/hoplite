package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.secrets.SecretsPolicy
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class Preprocessing(
  private val preprocessors: List<Preprocessor>,
  private val iterations: Int,
) {

  fun preprocess(node: Node): ConfigResult<Node> {
    return iterate(node, iterations)
  }

  private fun iterate(node: Node, iterations: Int): Validated<ConfigFailure, Node> =
    if (iterations == 0) node.valid() else process(node).flatMap { iterate(it, iterations - 1) }

  private fun process(node: Node): Validated<ConfigFailure, Node> =
    preprocessors.fold<Preprocessor, ConfigResult<Node>>(node.valid()) { acc, preprocessor ->
      acc.flatMap { preprocessor.process(it) }
    }
}

class Decoding(
  private val decoderRegistry: DecoderRegistry,
  private val secretsPolicy: SecretsPolicy?,
) {
  fun <A : Any> decode(kclass: KClass<A>, node: Node, mode: DecodeMode, context: DecoderContext): ConfigResult<A> {
    return decoderRegistry.decoder(kclass)
      .flatMap { it.decode(node, kclass.createType(), context) }
      .flatMap {
        DecodeModeValidator(mode).validate(
          it,
          createDecodingState(node, context, secretsPolicy)
        )
      }
  }
}

fun createDecodingState(
  root: Node,
  context: DecoderContext,
  secretsPolicy: SecretsPolicy?,
): DecodingState {
  val (used, unused) = root.paths()
    .filterNot { it.first == DotPath.root }
    .partition { context.usedPaths.contains(it.first) }
  return DecodingState(root, used, unused, createNodeStates(root, context, secretsPolicy))
}

private fun createNodeStates(
  root: Node,
  context: DecoderContext,
  secretsPolicy: SecretsPolicy?,
): List<NodeState> {
  return root.traverse().map { node ->

    val state = context.used.find { it.node.path == node.path }

    val secret = secretsPolicy?.isSecret(node, state?.type) ?: false

    NodeState(
      node = node,
      used = state?.used ?: false,
      value = state?.value,
      type = state?.type,
      secret = secret,
    )
  }
}

data class DecodingState(
  val root: Node,
  val used: List<Pair<DotPath, Pos>>,
  val unused: List<Pair<DotPath, Pos>>,
  val states: List<NodeState>,
)
