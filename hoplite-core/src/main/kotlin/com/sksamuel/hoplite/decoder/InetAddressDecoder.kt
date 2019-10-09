package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.net.InetAddress
import kotlin.reflect.KType

class InetAddressDecoder : NonNullableDecoder<InetAddress> {
  override fun supports(type: KType): Boolean = type.classifier == InetAddress::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<InetAddress> = when (node) {
    is StringValue -> Try { InetAddress.getByName(node.value) }.toValidated { ThrowableFailure(it) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

