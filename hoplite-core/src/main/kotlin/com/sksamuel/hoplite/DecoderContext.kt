package com.sksamuel.hoplite

import com.sksamuel.hoplite.secrets.SecretStrength
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.Validated
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Contains the configuration needed for decoders to work. For example, the context allows access to the
 * [DecoderRegistry] through which decoders can look up other decoders to be used for nested types.
 */
data class DecoderContext(
  val decoders: DecoderRegistry,
  val paramMappers: List<ParameterMapper>,
  // these are the dot paths for every config value - overrided or not, that was used
  val usedPaths: MutableSet<DotPath> = mutableSetOf(),
  // this tracks the types that a node was marshalled into
  val used: MutableSet<NodeState> = mutableSetOf(),
  val flattenArraysToString: Boolean = false,
) {

  /**
   * Returns a [Decoder] for type [type].
   */
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)

  /**
   * Returns a [Decoder] for type [KParameter].
   */
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoder(type.type)

  fun used(node: Node, type: KType, value: Any?) {
    this.used.add(NodeState(node, true, value, type, false, null))
  }

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), mutableSetOf())
  }
}

data class NodeState(
  val node: Node,
  val used: Boolean,
  val value: Any?, // the value assigned when this node was used
  val type: KType?,
  val secret: Boolean = false, // if this node is a secret
  val secretStrength: SecretStrength? = null, // if this node is a secret, then the strength
)
