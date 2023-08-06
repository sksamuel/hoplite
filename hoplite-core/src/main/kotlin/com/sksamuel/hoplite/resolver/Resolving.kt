package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.valueOrNull
import kotlin.reflect.KClass

/**
 * When a value is retrieved from the [Node] tree, before it is applied to the config, it is
 * passed through a series of [Resolver]s to allow for runtime modifications.
 *
 * Resolvers are recursively applied to allow for nesting.
 * Eg, myvalue: ${{ substitution-${{ env.prod }} }}
 */
class Resolving(
  private val resolvers: List<Resolver>,
  private val root: Node
) {

  suspend fun resolve(node: Node, paramName: String, kclass: KClass<*>, context: DecoderContext): ConfigResult<Node> {
    return resolvers.fold<Resolver, ConfigResult<Node>>(node.valid()) { accOrInvalid, resolver ->
      accOrInvalid.flatMap { acc ->
        resolver.resolve(paramName, kclass, acc, root, context).flatMap { resolved ->
          // if the resolver actually made a change we should run the resolver again on the result to allow
          // for repeated invocations / nested expressions
          if (resolved.valueOrNull() == acc.valueOrNull()) acc.valid() else resolve(
            resolved,
            paramName,
            kclass,
            context
          )
        }
      }
    }
  }

  companion object {
    val empty = Resolving(emptyList(), Undefined)
  }
}
