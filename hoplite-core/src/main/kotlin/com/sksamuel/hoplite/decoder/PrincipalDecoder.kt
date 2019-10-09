package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import java.security.Principal
import kotlin.reflect.KType

class PrincipalDecoder : NonNullableDecoder<Principal> {
  override fun supports(type: KType): Boolean = type.classifier == Principal::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Principal> = when (node) {
    is StringValue -> BasicPrincipal(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

data class BasicPrincipal(private val name: String) : Principal {
  override fun getName(): String = name
}
