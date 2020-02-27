package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.preprocessor.StringNodePreprocessor

object ParameterStorePreprocessor : StringNodePreprocessor() {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }
  private val regex = "\\$\\{ssm:(.+?)}".toRegex()

  private fun fetchParameterStoreValue(key: String): Try<String> = Try {
    val req = GetParameterRequest().withName(key).withWithDecryption(true)
    client.getParameter(req).parameter.value
  }

  override fun map(node: StringNode): Node {
    return when (val match = regex.matchEntire(node.value)) {
      null -> node
      else -> {
        val key = match.groupValues[1]
        val value = fetchParameterStoreValue(key).getOrElse { throw it }
        node.copy(value = value)
      }
    }
  }
}
