package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KType

fun <T> viaString(node: Node, path: String, type: KType, f: (String) -> T): ConfigResult<T> {
  return when (node) {
    is StringNode -> f(node.value).valid()
    else -> ConfigFailure.TypeConversionFailure(node, path, type).invalidNel()
  }
}

class KerberosPrincipalDecoder : NonNullableDecoder<KerberosPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<KerberosPrincipal> =
    viaString(node, path, type) { KerberosPrincipal(it) }
}

class JMXPrincipalDecoder : NonNullableDecoder<JMXPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == JMXPrincipal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<JMXPrincipal> =
    viaString(node, path, type) { JMXPrincipal(it) }
}

class X500PrincipalDecoder : NonNullableDecoder<X500Principal> {
  override fun supports(type: KType): Boolean = type.classifier == X500Principal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<X500Principal> =
    viaString(node, path, type) { X500Principal(it) }
}
