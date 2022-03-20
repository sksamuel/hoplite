package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

/**
 * Loads all config keys under the given path and then makes them available for substitutions.
 *
 * Note: If the values are encrypted, you must enable withDecryption inside the configure block.
 *
 * @param path the path to use.
 * @param configure the AWS request object for custom configuration. Optional.
 *
 */
class ParameterStorePathPreprocessor(
  private val path: String,
  private val configure: GetParametersByPathRequest.() -> Unit
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }
  private val regex = "\\$\\{ssm:(.+?)}".toRegex()

  private fun fetchParameterStoreValues(): Result<List<Parameter>> = runCatching {
    val req = GetParametersByPathRequest().withPath(path).apply(configure)
    client.getParametersByPath(req).parameters
  }

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
        null -> node.valid()
        else -> {
          val key = match.groupValues[1]
          fetchParameterStoreValues().fold(
            { values ->
              when (val value = values.firstOrNull { it.name == key }) {
                null -> ConfigFailure.PreprocessorError("Could not find key: $key in paths: ${values.map { it.name }}")
                  .invalid()
                else -> node.copy(value = value.value).valid()
              }
            },
            { ConfigFailure.PreprocessorFailure("Failed to load parameters", it).invalid() }
          )
        }
      }
    }
    else -> node.valid()
  }
}
