package com.sksamuel.hoplite.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

object ParameterStorePreprocessor : TraversingPrimitivePreprocessor() {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }
  private val regex1 = "\\$\\{ssm:(.+?)}".toRegex()
  private val regex2 = "paramstore://(.+?)".toRegex()

  private fun fetchParameterStoreValue(key: String): Result<String> = runCatching {
    val req = GetParameterRequest().withName(key).withWithDecryption(true)
    client.getParameter(req).parameter.value
  }

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (val match = regex1.matchEntire(node.value) ?: regex2.matchEntire(node.value)) {
        null -> node.valid()
        else -> {
          val key = match.groupValues[1]
          fetchParameterStoreValue(key).fold(
            { node.copy(value = it).valid() },
            { ConfigFailure.PreprocessorFailure("Could not load '$key' from parameter store", it).invalid() }
          )
        }
      }
    }
    else -> node.valid()
  }
}
