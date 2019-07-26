package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KType

fun <T> viaString(cursor: Node, f: (String) -> T): ConfigResult<T> {
  return cursor.string().map { f(it) }
}

class KerberosPrincipalDecoder : BasicDecoder<KerberosPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun decode(node: Node): ConfigResult<KerberosPrincipal> =
      viaString(node) { KerberosPrincipal(it) }
}

class JMXPrincipalDecoder : BasicDecoder<JMXPrincipal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun decode(node: Node): ConfigResult<JMXPrincipal> =
      viaString(node) { JMXPrincipal(it) }
}

class X500PrincipalDecoder : BasicDecoder<X500Principal> {
  override fun supports(type: KType): Boolean = type.classifier == KerberosPrincipal::class
  override fun decode(node: Node): ConfigResult<X500Principal> =
      viaString(node) { X500Principal(it) }
}