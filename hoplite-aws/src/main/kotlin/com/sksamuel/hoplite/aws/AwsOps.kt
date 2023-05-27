package com.sksamuel.hoplite.aws

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.amazonaws.services.secretsmanager.model.InvalidParameterException
import com.amazonaws.services.secretsmanager.model.LimitExceededException
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AwsOps(private val client: AWSSecretsManager) {

  companion object {
    const val ReportSection = "AWS Secrets Manager Lookups"

    // check for index, so we can decode json stored in AWS
    val keyRegex = "(.+)\\[(.+)]".toRegex()
  }

  fun report(context: DecoderContext, result: GetSecretValueResult) {
    context.reporter.report(
      ReportSection,
      mapOf(
        "Name" to result.name,
        "Arn" to result.arn,
        "Created Date" to result.createdDate.toString(),
        "Version Id" to result.versionId
      )
    )
  }

  fun extractIndex(path: String): Pair<String, String?> {
    val keyMatch = keyRegex.matchEntire(path)
    return if (keyMatch == null)
      Pair(path, null)
    else
      Pair(keyMatch.groupValues[1], keyMatch.groupValues[2])
  }

  fun parseSecret(result: GetSecretValueResult, index: String?): ConfigResult<String> {

    val secret = result.secretString
    return if (secret.isNullOrBlank())
      ConfigFailure.PreprocessorWarning("Empty secret '${result.name}' in AWS SecretsManager").invalid()
    else {
      if (index == null) {
        secret.valid()
      } else {
        val map = runCatching { Json.Default.decodeFromString<Map<String, String>>(secret) }.getOrElse { emptyMap() }
        map[index]?.valid()
          ?: ConfigFailure.ResolverError(
            "Index '$index' not present in AWS secret '${result.name}'. Present keys are ${map.keys.joinToString(",")}"
          ).invalid()
      }
    }
  }

  fun fetchSecret(key: String): ConfigResult<GetSecretValueResult> {
    return try {
      val req = GetSecretValueRequest().withSecretId(key)
      client.getSecretValue(req).valid()
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
