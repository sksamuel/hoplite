package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.toValidated
import java.net.URI
import java.net.URL
import kotlin.reflect.KType

class URLDecoder : NonNullableDecoder<URL> {
  override fun supports(type: KType): Boolean = type.classifier == URL::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<URL> = when (node) {
    is StringValue -> Try { URL(node.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class URIDecoder : NonNullableDecoder<URI> {
  override fun supports(type: KType): Boolean = type.classifier == URI::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<URI> = when (node) {
    is StringValue -> Try { URI.create(node.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
