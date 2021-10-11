package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

object ParameterStorePreprocessor : TraversingPrimitivePreprocessor() {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }
  private val regex = "\\$\\{ssm:(.+?)}".toRegex()

  private fun fetchParameterStoreValue(key: String): Result<String> = runCatching {
    val req = GetParameterRequest().withName(key).withWithDecryption(true)
    client.getParameter(req).parameter.value
  }

  override fun handle(node: PrimitiveNode): Node = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
        null -> node
        else -> {
          val key = match.groupValues[1]
          val value = fetchParameterStoreValue(key)
            .getOrElse { throw ConfigException("Failed loading parameter key '$key'", it) }
          node.copy(value = value)
        }
      }
    }
    else -> node
  }
}
