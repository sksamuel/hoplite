package com.sksamuel.hoplite.aws.kotlin

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.getParameter
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import kotlinx.coroutines.runBlocking

object ParameterStorePreprocessor : TraversingPrimitivePreprocessor() {

  private val client by lazy { runBlocking { SsmClient.fromEnvironment() } }
  private val regex1 = "\\$\\{ssm:(.+?)}".toRegex()
  private val regex2 = "paramstore://(.+?)".toRegex()

  private fun fetchParameterStoreValue(key: String): Result<String> = runCatching {
    runBlocking {
      client.getParameter {
        name = key
        withDecryption = true
      }.parameter?.value ?: throw RuntimeException("Parameter with key: $key not found")
    }
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
