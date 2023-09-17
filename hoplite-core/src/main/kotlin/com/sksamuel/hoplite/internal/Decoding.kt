package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NodeState
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.paths
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.traverse
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class Decoding(
  private val decoderRegistry: DecoderRegistry,
  private val secretsPolicy: SecretsPolicy?
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

internal fun createDecodingState(
  root: Node,
  context: DecoderContext,
  secretsPolicy: SecretsPolicy?
): DecodingState {
  val (used, unused) = root.paths()
    .filterNot { it.first == DotPath.root }
    .partition { context.usedPaths.contains(it.first) }
  return DecodingState(root, used, unused, createNodeStates(root, context, secretsPolicy))
}

private fun createNodeStates(
  root: Node,
  context: DecoderContext,
  secretsPolicy: SecretsPolicy?
): List<NodeState> {
  return root.traverse().map { node ->

    val state = context.used.entries.find { it.key == node.path }?.value

    val secret = secretsPolicy?.isSecret(node, state?.type) ?: false

    NodeState(
      node = node,
      used = state?.used ?: false,
      value = state?.value,
      type = state?.type,
      secret = secret
    )
  }
}

data class DecodingState(
  val root: Node,
  val used: List<Pair<DotPath, Pos>>,
  val unused: List<Pair<DotPath, Pos>>,
  val states: List<NodeState>
)
