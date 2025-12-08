package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecodedPath
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NodeState
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.decodedPaths
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
  val (used, unused) = root.decodedPaths()
    .filterNot { it.path == DotPath.root }
    .partition { context.usedPaths.contains(it.path) || it.isClassDiscriminator(context) }
  return DecodingState(root, used, unused, createNodeStates(root, context, secretsPolicy))
}

internal fun DecodedPath.isClassDiscriminator(context: DecoderContext) : Boolean {
  return if(sourceKey != null && context.sealedTypeDiscriminatorField != null) {
    sourceKey.endsWith(".${context.sealedTypeDiscriminatorField}")
  } else {
    false
  }
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
  val used: List<DecodedPath>,
  val unused: List<DecodedPath>,
  val states: List<NodeState>
)
