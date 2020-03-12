package com.sksamuel.hoplite.decoder.javax

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KType

fun <T> viaString(node: Node, type: KType, f: (String) -> T): ConfigResult<T> {
  return when (node) {
    is StringNode -> f(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class KerberosPrincipalDecoder : NullHandlingDecoder<KerberosPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<KerberosPrincipal> =
    viaString(node, type) { KerberosPrincipal(it) }
}

class JMXPrincipalDecoder : NullHandlingDecoder<JMXPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == JMXPrincipal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<JMXPrincipal> =
    viaString(node, type) { JMXPrincipal(it) }
}

class X500PrincipalDecoder : NullHandlingDecoder<X500Principal> {
  override fun supports(type: KType): Boolean = type.classifier == X500Principal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<X500Principal> =
    viaString(node, type) { X500Principal(it) }
}
