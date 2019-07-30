package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.toValidated
import java.net.URI
import java.net.URL
import kotlin.reflect.KType

class URLDecoder : NonNullableDecoder<URL> {
  override fun supports(type: KType): Boolean = type.classifier == URL::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<URL> = when (node) {
    is StringNode -> Try { URL(node.value) }.toValidated { ConfigFailure.DecodeError(node, path, type) }
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}

class URIDecoder : NonNullableDecoder<URI> {
  override fun supports(type: KType): Boolean = type.classifier == URI::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<URI> = when (node) {
    is StringNode -> Try { URI.create(node.value) }.toValidated { ConfigFailure.DecodeError(node, path, type) }
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}
