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
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.PrefixResolver
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.withMeta
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Replaces strings of the form awssecretsmanager:// by looking up the path in AWS Secrets Manager.
 * Defaults can also be applied in case the path does not exist: awssecretsmanager://path :- default
 */
class AwsSecretsManagerPrefixResolver(
  private val report: Boolean = false,
  private val createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() },
) : PrefixResolver() {

  private val client by lazy { createClient() }

  override val prefix = "awssecretsmanager://"

  // check for index, so we can decode json stored in AWS
  private val keyRegex = "(.+)\\[(.+)]".toRegex()

  override fun resolve(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<StringNode> {
    val keyMatch = keyRegex.matchEntire(path)
    val (key, index) = if (keyMatch == null)
      Pair(path, null)
    else
      Pair(
        keyMatch.groupValues[1],
        keyMatch.groupValues[2]
      )
    return fetchSecret(key, index, node, context)
  }

  private fun fetchSecret(
    key: String,
    index: String?,
    node: StringNode,
    context: DecoderContext
  ): ConfigResult<StringNode> {
    return try {

      val req = GetSecretValueRequest().withSecretId(key)
      val value = client.getSecretValue(req)

      if (report)
        context.report(
          ReportSection,
          mapOf(
            "Name" to value.name,
            "Arn" to value.arn,
            "Created Date" to value.createdDate.toString(),
            "Version Id" to value.versionId,
          )
        )

      val secret = value.secretString
      if (secret.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty secret '$key' in AWS SecretsManager").invalid()
      else {
        if (index == null) {
          context.addMetaData(CommonMetadata.RemoteLookup, value)
          node.copy(value = secret)
            .withMeta(CommonMetadata.Secret, true)
            .withMeta(CommonMetadata.UnprocessedValue, node.value)
            .withMeta(CommonMetadata.RemoteLookup, "AWS '$key'")
            .valid()
        } else {
          val map = runCatching { Json.Default.decodeFromString<Map<String, String>>(secret) }.getOrElse { emptyMap() }
          val indexedValue = map[index]
          if (indexedValue == null)
            ConfigFailure.PreprocessorWarning(
              "Index '$index' not present in secret '$key'. Available keys are ${map.keys.joinToString(",")}"
            ).invalid()
          else
            node.copy(value = indexedValue)
              .withMeta(CommonMetadata.Secret, true)
              .withMeta(CommonMetadata.UnprocessedValue, node.value)
              .withMeta(CommonMetadata.RemoteLookup, "AWS '$key[$index]'")
              .valid()
        }
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

  companion object {
    const val ReportSection = "AWS Secrets Manager Lookups"
  }
}
