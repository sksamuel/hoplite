package com.sksamuel.hoplite.aws

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.InvalidParameterException
import com.amazonaws.services.secretsmanager.model.LimitExceededException
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.withMeta

class AwsSecretsManagerPreprocessor(
  private val createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex1 = "\\$\\{awssecret:(.+?)}".toRegex()
  private val regex2 = "secretsmanager://(.+?)".toRegex()
  private val regex3 = "awssm://(.+?)".toRegex()

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (
        val match = regex1.matchEntire(node.value) ?: regex2.matchEntire(node.value) ?: regex3.matchEntire(node.value)
      ) {
        null -> node.valid()
        else -> fetchSecret(match.groupValues[1].trim(), node)
      }
    }
    else -> node.valid()
  }

  private fun fetchSecret(key: String, node: StringNode): ConfigResult<Node> {
    return try {
      val req = GetSecretValueRequest().withSecretId(key)
      val value = client.getSecretValue(req).secretString
      if (value.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty secret '$key' in AWS SecretsManager").invalid()
      else {
        val copied = node.copy(value = value)
          .withMeta(CommonMetadata.IsSecretLookup, true)
          .withMeta(CommonMetadata.UnprocessedValue, node.value)
          .withMeta(CommonMetadata.RemoteLookup, "AWS Secrets Manager '$key'")
        copied.valid()
      }
    } catch (e: ResourceNotFoundException) {
      ConfigFailure.PreprocessorWarning("Could not locate resource '$key' in AWS SecretsManager").invalid()
    } catch (e: DecryptionFailureException) {
      ConfigFailure.PreprocessorWarning("Could not decrypt resource '$key' in AWS SecretsManager").invalid()
    } catch (e: LimitExceededException) {
      ConfigFailure.PreprocessorWarning("Could not load resource '$key' due to limits exceeded").invalid()
    } catch (e: InvalidParameterException) {
      ConfigFailure.PreprocessorWarning("Invalid parameter name '$key' in AWS SecretsManager").invalid()
    } catch (e: AmazonServiceException) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    } catch (e: AmazonClientException) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    }
  }
}
