package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.Try
import java.net.URI
import java.net.URL
import kotlin.reflect.KType

class URLDecoder : NullHandlingDecoder<URL> {
  override fun supports(type: KType): Boolean = type.classifier == URL::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<URL> = when (node) {
    is StringNode -> Try { URL(node.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class URIDecoder : NullHandlingDecoder<URI> {
  override fun supports(type: KType): Boolean = type.classifier == URI::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<URI> = when (node) {
    is StringNode -> Try { URI.create(node.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
