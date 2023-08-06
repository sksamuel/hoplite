package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import kotlin.reflect.KClass

/**
 * A [Resolver] is used to map or filter a value before that value is applied to a field.
 *
 * For example, a resolver may apply substitutions from elsewhere in the tree (see [ReferenceContextResolver]),
 * or apply environmental variable overrides (see [EnvVarContextResolver]), or it may resolve secrets by looking
 * up values in an external secrets store such as Hashicorp Vault or AWS Secrets Manager.
 *
 * For a full list of out of the box resolvers see project documentation at
 * https://github.com/sksamuel/hoplite
 */
interface Resolver {

  /**
   * Invokes this [Resolver] against the given [node].
   *
   * The [root] node is provided if the resolver wishes to access to the parsed tree.
   * The [context] is provided for access to decoders and to add to reports.
   *
   * The resolver should return the modified node, or if no resolution was applied,
   * then the unmodified input node.
   *
   * In the case of an error, an invalid [ConfigFailure] should be returned.
   *
   * @param paramName the name of the field as defined in the target data class.
   * @param kclass the [KClass] of the target data class.
   * @param node the node that contains the value that will be applied to the field
   * @param root the root node tree
   * @param context provides access to the decoding engine.
   */
  suspend fun resolve(
    paramName: String?,
    kclass: KClass<*>,
    node: Node,
    root: Node,
    context: DecoderContext
  ): ConfigResult<Node>
}
