package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KType

fun <T> viaString(cursor: Value, f: (String) -> T): ConfigResult<T> {
  return cursor.string().map { f(it) }
}

class SecurityDecoderRegistration : DecoderRegistration {
  override fun register(registry: DecoderRegistry) {
    registry.register(KerberosPrincipal::class, KerberosPrincipalDecoder)
    registry.register(X500Principal::class, X500PrincipalDecoder)
    registry.register(JMXPrincipal::class, JMXPrincipalDecoder)
  }
}

object KerberosPrincipalDecoder : Decoder<KerberosPrincipal> {
  override fun convert(value: Value, type: KType): ConfigResult<KerberosPrincipal> =
      viaString(value) { KerberosPrincipal(it) }
}

object JMXPrincipalDecoder : Decoder<JMXPrincipal> {
  override fun convert(value: Value, type: KType): ConfigResult<JMXPrincipal> =
      viaString(value) { JMXPrincipal(it) }
}

object X500PrincipalDecoder : Decoder<X500Principal> {
  override fun convert(value: Value, type: KType): ConfigResult<X500Principal> =
      viaString(value) { X500Principal(it) }
}