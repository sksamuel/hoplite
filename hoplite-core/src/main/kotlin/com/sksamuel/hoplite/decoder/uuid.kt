package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.toValidated
import java.util.*
import kotlin.reflect.KType

class UUIDDecoder : NonNullableDecoder<UUID> {
  override fun supports(type: KType): Boolean = type.classifier == UUID::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<UUID> = when (node) {
    is StringValue ->
      Try { UUID.fromString(node.value) }
        .toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
