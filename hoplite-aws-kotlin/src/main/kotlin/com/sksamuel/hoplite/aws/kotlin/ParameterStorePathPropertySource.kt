package com.sksamuel.hoplite.aws.kotlin

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParametersByPathRequest
import aws.sdk.kotlin.services.ssm.model.Parameter
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.parsers.toNode
import kotlinx.coroutines.runBlocking

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
  private val configure: GetParametersByPathRequest.Builder.() -> kotlin.Unit
) : PropertySource {

  override fun source(): String = "AWS SSM Parameter Store"

  private val client by lazy { runBlocking { SsmClient.fromEnvironment() } }

  private fun fetchParameterStoreValues(): Result<List<Parameter>> = runCatching {
    val req = GetParametersByPathRequest {
      path = prefix
      configure()
    }
    val params = mutableListOf<Parameter>()
    tailrec fun go(request: GetParametersByPathRequest, parameters: MutableList<Parameter>): List<Parameter> {
      val result = runBlocking { client.getParametersByPath(request) }
      val resultParams = result.parameters ?: emptyList()
      return if (result.nextToken != null) {
        val nextReq = req.copy {
          nextToken = result.nextToken
        }
        go(nextReq, (parameters + resultParams).toMutableList())
      } else resultParams + parameters
    }
    go(req, params)
  }

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return fetchParameterStoreValues().map { params ->
      params.associate { it.name!! to it.value }.toNode("aws_parameter_store at $prefix", "/") {
        (if (stripPath) it.removePrefix(prefix) else it).removePrefix("/")
      }
    }.toValidated {
      ConfigFailure.PropertySourceFailure("Could not fetch data from AWS parameter store: ${it.message}", it)
    }
  }
}
