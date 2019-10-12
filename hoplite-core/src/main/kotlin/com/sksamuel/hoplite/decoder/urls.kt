package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.net.URI
import java.net.URL
import kotlin.reflect.KType

class URLDecoder : NonNullableDecoder<URL> {
  override fun supports(type: KType): Boolean = type.classifier == URL::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<URL> = when (val v = node.value) {
    is Value.StringNode -> Try { URL(v.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class URIDecoder : NonNullableDecoder<URI> {
  override fun supports(type: KType): Boolean = type.classifier == URI::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<URI> = when (val v = node.value) {
    is Value.StringNode -> Try { URI.create(v.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
