package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.toValidated
import java.net.URI
import java.net.URL
import kotlin.reflect.KType

class URLDecoder : Decoder<URL> {
  override fun supports(type: KType): Boolean = type.classifier == URL::class
  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<URL> = when (node) {
    is StringNode -> Try { URL(node.value) }.toValidated {
      ConfigFailure.TypeConversionFailure(node, path, type)
    }.toValidatedNel()
    else -> ConfigFailure.conversionFailure<URL>(node).invalidNel()
  }
}

class URIDecoder : Decoder<URI> {
  override fun supports(type: KType): Boolean = type.classifier == URI::class
  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<URI> = when (node) {
    is StringNode -> Try { URI.create(node.value) }.toValidated {
      ConfigFailure.TypeConversionFailure(node, path, type)
    }.toValidatedNel()
    else -> ConfigFailure.conversionFailure<Short>(node).invalidNel()
  }
}
