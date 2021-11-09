package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

/**
 * Provides all keys under a path as config values.
 *
 * Note: If the values are encrypted, you must enable withDecryption inside the configure block.
 *
 * @param path the path to use.
 * @param configure the AWS request object for custom configuration. Optional.
 *
 */
class ParameterStorePathPropertySource(
  private val path: String,
  private val configure: GetParametersByPathRequest.() -> Unit
) : PropertySource {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }

  private fun fetchParameterStoreValues(): Result<List<Parameter>> = runCatching {
    val req = GetParametersByPathRequest().withPath(path).apply(configure)
    client.getParametersByPath(req).parameters
  }

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return fetchParameterStoreValues().map { params ->
      val map = params.associate { param -> param.name to StringNode(param.value, Pos.NoPos) }
      MapNode(map, Pos.NoPos)
    }.fold(
      { it.valid() },
      {
        ConfigFailure.PropertySourceFailure("Could not fetch data from AWS parameter store: ${it.message}").invalid()
      }
    )
  }
}
