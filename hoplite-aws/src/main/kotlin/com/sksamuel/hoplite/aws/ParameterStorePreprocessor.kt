package com.sksamuel.hoplite.aws

import arrow.core.Try
import arrow.core.getOrElse
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.sksamuel.hoplite.preprocessor.Preprocessor

object ParameterStorePreprocessor : Preprocessor {

  private val client by lazy { AWSSimpleSystemsManagementClientBuilder.defaultClient() }
  private val regex = "\\$\\{paramstore:(.+?)}".toRegex()

  private fun fetchParameterStoreValue(key: String): Try<String> = Try {
    val req = GetParameterRequest().withName(key).withWithDecryption(true)
    client.getParameter(req).parameter.value
  }

  override fun process(value: String): String {
    return when (val match = regex.matchEntire(value)) {
      null -> value
      else -> {
        val key = match.groupValues[1]
        fetchParameterStoreValue(key).getOrElse { throw it }
      }
    }
  }
}
