package com.sksamuel.hoplite.aws.kotlin

import aws.sdk.kotlin.runtime.AwsServiceException
import aws.sdk.kotlin.runtime.ClientException
import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import aws.sdk.kotlin.services.secretsmanager.model.DecryptionFailure
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueResponse
import aws.sdk.kotlin.services.secretsmanager.model.InvalidParameterException
import aws.sdk.kotlin.services.secretsmanager.model.LimitExceededException
import aws.sdk.kotlin.services.secretsmanager.model.ResourceNotFoundException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AwsOps(private val client: SecretsManagerClient) {

  companion object {
    const val ReportSection = "AWS Secrets Manager Lookups"

    // check for index, so we can decode json stored in AWS
    val keyRegex = "(.+)\\[(.+)]".toRegex()
  }

  fun report(context: DecoderContext, result: GetSecretValueResponse) {
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

  fun parseSecret(result: GetSecretValueResponse, index: String?): ConfigResult<String> {

    val secret = result.secretString
    return if (secret.isNullOrBlank())
      ConfigFailure.PreprocessorWarning("Empty secret '${result.name}' in AWS SecretsManager").invalid()
    else {
      if (index == null) {
        secret.valid()
      } else {
        val map = runCatching { Json.Default.decodeFromString<Map<String, String>>(secret) }.getOrElse { emptyMap() }
        map[index]?.valid()
          ?: ConfigFailure.ResolverFailure(
            "Index '$index' not present in AWS secret '${result.name}'. Present keys are ${map.keys.joinToString(",")}"
          ).invalid()
      }
    }
  }

  fun fetchSecret(key: String): ConfigResult<GetSecretValueResponse> {
    return try {
      runBlocking {
        client.getSecretValue {
          secretId = key
        }
      }.valid()
    } catch (e: ResourceNotFoundException) {
      ConfigFailure.PreprocessorWarning("Could not locate resource '$key' in AWS SecretsManager").invalid()
    } catch (e: DecryptionFailure) {
      ConfigFailure.PreprocessorWarning("Could not decrypt resource '$key' in AWS SecretsManager").invalid()
    } catch (e: LimitExceededException) {
      ConfigFailure.PreprocessorWarning("Could not load resource '$key' due to limits exceeded").invalid()
    } catch (e: InvalidParameterException) {
      ConfigFailure.PreprocessorWarning("Invalid parameter name '$key' in AWS SecretsManager").invalid()
    } catch (e: AwsServiceException) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    } catch (e: ClientException) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    }
  }
}
