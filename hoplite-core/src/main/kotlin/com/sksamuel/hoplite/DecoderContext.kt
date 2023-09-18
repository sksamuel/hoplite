package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import com.sksamuel.hoplite.resolver.Resolving
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * A context object used during the preprocessing and decode stages.
 *
 * Contains access to the [DecoderRegistry] and [ParameterMapper]s and configuration settings.
 * Can be used to add metadata about the decoding process.
 *
 * Decoders should use this object to report which nodes have been used.
 */
data class DecoderContext(
  val decoders: DecoderRegistry,
  val paramMappers: List<ParameterMapper>,
  val reporter: Reporter = Reporter(),
  // these are the dot paths for every config value - overrided or not, that was used
  val usedPaths: MutableSet<DotPath> = mutableSetOf(),
  // this tracks the types that a node was marshalled into
  val used: MutableMap<DotPath, NodeState> = mutableMapOf(),
  val metadata: MutableMap<String, Any?> = mutableMapOf(),
  val config: DecoderConfig = DecoderConfig(false),
  val environment: Environment? = null,
  val resolvers: Resolving = Resolving.empty,
  // determines if we should error when a context resolver cannot find a substitution
  val contextResolverMode: ContextResolverMode = ContextResolverMode.ErrorOnUnresolved,
  val sealedTypeDiscriminatorField: String? = null,
) {

  /**
   * Returns a [Decoder] for type [type].
   */
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)

  /**
   * Returns a [Decoder] for type [KParameter].
   */
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoder(type.type)

  /**
   * Makes a node as marshalled into the given [type] with the resolved value [value].
   */
  fun used(node: Node, type: KType, value: Any?) {
    this.used[node.path] = NodeState(node, true, value, type)
  }

  /**
   * Adds a [row] to the named [section] in the report.
   */
  @Deprecated("Use reporter.report", ReplaceWith("reporter.report(section, row)"))
  fun report(section: String, row: Map<String, Any?>) = reporter.report(section, row)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), resolvers = Resolving(emptyList(), Undefined))
  }
}

data class NodeState(
  val node: Node,
  val used: Boolean,
  val value: Any?, // the value assigned when this node was used
  val type: KType?,
  val secret: Boolean = false, // if this node is a secret
)

data class DecoderConfig(
  val flattenArraysToString: Boolean
)
