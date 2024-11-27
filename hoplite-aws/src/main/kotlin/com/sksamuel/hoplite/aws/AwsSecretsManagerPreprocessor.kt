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
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.withMeta
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * @param report set to true to output a report on secrets used.
 *               Requires the overall hoplite report to be enabled.
 */
class AwsSecretsManagerPreprocessor(
  private val report: Boolean = false,
  private val json: Json,
  private val createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : TraversingPrimitivePreprocessor() {

  constructor(
    report: Boolean = false,
    createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
  ) : this(report, Json.Default, createClient)

  private val client by lazy { createClient() }
  private val regex1 = "\\$\\{awssecret:(.+?)}".toRegex()
  private val regex2 = "secretsmanager://(.+?)".toRegex()
  private val regex3 = "awssm://(.+?)".toRegex()
  private val keyRegex = "(.+)\\[(.+)]".toRegex()

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (
        val match = regex1.matchEntire(node.value) ?: regex2.matchEntire(node.value) ?: regex3.matchEntire(node.value)
      ) {
        null -> node.valid()
        else -> {
          val value = match.groupValues[1].trim()
          val keyMatch = keyRegex.matchEntire(value)
          val (key, index) = if (keyMatch == null) Pair(value, null) else
            Pair(keyMatch.groupValues[1], keyMatch.groupValues[2])
          context.reportPrintFn("[WARN] Use of the hoplite-aws module is deprecated. Please use the hoplite-aws2 module instead.")
          fetchSecret(key, index, node, context)
        }
      }
    }
    else -> node.valid()
  }

  private fun fetchSecret(key: String, index: String?, node: StringNode, context: DecoderContext): ConfigResult<Node> {
    return try {

      val req = GetSecretValueRequest().withSecretId(key)
      val value = client.getSecretValue(req)

      if (report)
        context.reporter.report(
          "AWS Secrets Manager Lookups",
          mapOf(
            "Name" to value.name,
            "Arn" to value.arn,
            "Created Date" to value.createdDate.toString(),
            "Version Id" to value.versionId
          )
        )

      val secret = value.secretString
      if (secret.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty secret '$key' in AWS SecretsManager").invalid()
      else {
        if (index == null) {
          node.copy(value = secret)
            .withMeta(CommonMetadata.Secret, true)
            .withMeta(CommonMetadata.UnprocessedValue, node.value)
            .withMeta(CommonMetadata.RemoteLookup, "AWS '$key'")
            .valid()
        } else {
          val map = runCatching { json.decodeFromString<Map<String, String>>(secret) }.getOrElse { emptyMap() }
          val indexedValue = map[index]
          if (indexedValue == null)
            ConfigFailure.PreprocessorWarning(
              "Index '$index' not present in secret '$key'. Available keys are ${
                map.keys.joinToString(
                  ","
                )
              }"
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
}
