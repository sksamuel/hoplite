package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KType

fun <T> viaString(node: TreeNode, type: KType, f: (String) -> T): ConfigResult<T> {
  return when (val v = node.value) {
    is Value.StringNode -> f(v.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class KerberosPrincipalDecoder : NonNullableDecoder<KerberosPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<KerberosPrincipal> =
    viaString(node, type) { KerberosPrincipal(it) }
}

class JMXPrincipalDecoder : NonNullableDecoder<JMXPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == JMXPrincipal::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<JMXPrincipal> =
    viaString(node, type) { JMXPrincipal(it) }
}

class X500PrincipalDecoder : NonNullableDecoder<X500Principal> {
  override fun supports(type: KType): Boolean = type.classifier == X500Principal::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<X500Principal> =
    viaString(node, type) { X500Principal(it) }
}
