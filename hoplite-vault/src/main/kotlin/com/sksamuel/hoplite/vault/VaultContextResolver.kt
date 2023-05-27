package com.sksamuel.hoplite.vault

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Reporter
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.ContextResolver
import org.springframework.vault.core.VaultKeyValueOperationsSupport
import org.springframework.vault.core.VaultTemplate

class VaultContextResolver(
  private val createClient: () -> VaultTemplate,
  private val report: Boolean = false,
  private val apiVersion: VaultKeyValueOperationsSupport.KeyValueBackend = VaultKeyValueOperationsSupport.KeyValueBackend.KV_2
) : ContextResolver() {

  private val client by lazy { createClient() }

  override val contextKey: String = "vault"
  override val default: Boolean = false

  private val tokenRegex = "(.+)\\s+(.+)".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return when (val match = tokenRegex.matchEntire(path)) {
      null -> ConfigFailure.ResolverFailure("Must specify vault key at '${path}'").invalid()
      else -> fetchSecret(match.groupValues[1], match.groupValues[2], context.reporter)
    }
  }

  private fun fetchSecret(
    path: String,
    key: String,
    reporter: Reporter,
  ): ConfigResult<String> = runCatching {

    val paths = path.split("/")
    if (paths.size < 2) return ConfigFailure.ResolverFailure("Invalid vault path '$path'").invalid()

    val ops = client.opsForKeyValue(paths[0], apiVersion)
    val pathSecret = ops.get(paths.drop(1).joinToString("/"))
      ?: return ConfigFailure.ResolverFailure("Vault path '$path' not found").invalid()

    if (report) {
      reporter.report(
        "Vault Lookups",
        mapOf("Path" to path, "Key" to key, "Lease Id" to pathSecret.leaseId)
      )
    }

    val data = pathSecret.data
    if (data == null) {
      ConfigFailure.ResolverFailure("No data at path '$path' in Vault").invalid()
    } else {
      val value = data[key]
      value?.toString()?.valid()
        ?: ConfigFailure.ResolverFailure("Vault key '$key' not found in path '$path'").invalid()
    }
  }.getOrElse {
    ConfigFailure.ResolverException("Failed loading secret '$path $key' from Vault", it).invalid()
  }
}
