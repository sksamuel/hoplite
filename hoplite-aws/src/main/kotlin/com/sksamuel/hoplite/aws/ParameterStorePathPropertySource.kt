package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import java.util.Properties

/**
 * Provides all keys under a prefix path as config values.
 *
 * Note: If the values are encrypted, you must enable withDecryption inside the [configure] block.
 *
 * @param prefix the prefix for paths to retrieve.
 * @param stripPath if true then the prefix will be stripped from each key
 * @param configure the AWS request object for custom configuration. Optional.
 *
 */
class ParameterStorePathPropertySource(
  private val prefix: String,
  private val stripPath: Boolean = true,
  private val configure: GetParametersByPathRequest.() -> Unit
) : PropertySource {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }

  private fun fetchParameterStoreValues(): Result<List<Parameter>> = runCatching {
    val req = GetParametersByPathRequest().withPath(prefix).apply(configure)
    client.getParametersByPath(req).parameters
  }

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return fetchParameterStoreValues().map { params ->
      val props = Properties()
      params.forEach {
        val name = if (stripPath) it.name.removePrefix(prefix) else it.name
        props[name] = it.value
      }
      props.toNode("aws_parameter_store at $prefix", "/")
    }.fold(
      { it.valid() },
      {
        ConfigFailure.PropertySourceFailure("Could not fetch data from AWS parameter store: ${it.message}").invalid()
      }
    )
  }
}
