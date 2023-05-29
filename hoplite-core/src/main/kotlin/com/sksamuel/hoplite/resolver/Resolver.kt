package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node

/**
 * A [Resolver] is used to modify a value before the value is applied to a field in a data config class.
 *
 * For example, a resolver may apply substitutions from elsewhere in the tree (see [ReferenceContextResolver]),
 * or apply envvar overrides (see [EnvVarContextResolver]), or it may resolve against a secrets manager
 * such as Vault or AWS Secrets Manager.
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
   */
  suspend fun resolve(paramName: String?, node: Node, root: Node, context: DecoderContext): ConfigResult<Node>
}
