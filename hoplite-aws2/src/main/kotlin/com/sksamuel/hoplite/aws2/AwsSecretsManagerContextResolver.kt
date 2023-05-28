package com.sksamuel.hoplite.aws2

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.ContextResolver
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException
import software.amazon.awssdk.services.secretsmanager.model.LimitExceededException
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException

/**
 * @param report set to true to output a report on secrets used.
 *               Requires the overall hoplite report to be enabled.
 */
class AwsSecretsManagerContextResolver(
  private val report: Boolean = false,
  private val createClient: () -> SecretsManagerClient = { SecretsManagerClient.create() }
) : ContextResolver() {

  override val contextKey: String = "aws-secrets-manager"
  override val default: Boolean = false

  private val client by lazy { createClient() }
  private val keyRegex = "(.+)\\[(.+)]".toRegex()

  override fun lookup(
    path: String,
    node: StringNode,
    root: Node,
    context: DecoderContext
  ): ConfigResult<String?> {
    val keyMatch = keyRegex.matchEntire(path)
    val (key, index) = if (keyMatch == null) Pair(path, null) else
      Pair(keyMatch.groupValues[1], keyMatch.groupValues[2])
    return fetchSecret(key, index, context)
  }

  private fun fetchSecret(
    key: String,
    index: String?,
    context: DecoderContext
  ): ConfigResult<String?> {
    return try {

      val valueRequest = GetSecretValueRequest.builder().secretId(key).build()
      val value = client.getSecretValue(valueRequest)

      if (report)
        context.reporter.report(
          "AWS Secrets Manager Lookups",
          mapOf(
            "Name" to value.name(),
            "Arn" to value.arn(),
            "Created Date" to value.createdDate().toString(),
            "Version Id" to value.versionId()
          )
        )

      val secret = value.secretString()
      if (secret.isNullOrBlank())
        ConfigFailure.ResolverFailure("Empty secret '$key' in AWS SecretsManager").invalid()
      else {
        if (index == null) secret.valid()
        else {
          val map = runCatching { Json.Default.decodeFromString<Map<String, String>>(secret) }.getOrElse { emptyMap() }
          val indexedValue = map[index]
          val failureMsg = "Index '$index' not present in secret '$key'. Available keys: ${map.keys.joinToString(",")}"
          indexedValue?.valid() ?: ConfigFailure.ResolverFailure(failureMsg).invalid()
        }
      }
    } catch (e: ResourceNotFoundException) {
      ConfigFailure.ResolverFailure("Could not locate resource '$key' in AWS SecretsManager").invalid()
    } catch (e: DecryptionFailureException) {
      ConfigFailure.ResolverFailure("Could not decrypt resource '$key' in AWS SecretsManager").invalid()
    } catch (e: LimitExceededException) {
      ConfigFailure.ResolverFailure("Could not load resource '$key' due to limits exceeded").invalid()
    } catch (e: InvalidParameterException) {
      ConfigFailure.ResolverFailure("Invalid parameter name '$key' in AWS SecretsManager").invalid()
    } catch (e: SecretsManagerException) {
      ConfigFailure.ResolverException("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    } catch (e: Exception) {
      ConfigFailure.ResolverException("Failed loading secret '$key' from AWS SecretsManager", e).invalid()
    }
  }
}
