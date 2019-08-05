package com.sksamuel.hoplite.okhttp

import arrow.core.toOption
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.NonNullableDecoder
import com.sksamuel.hoplite.decoder.StringDecoder
import okhttp3.HttpUrl
import kotlin.reflect.KType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class HttpUrlDecoder : NonNullableDecoder<HttpUrl> {
  override fun supports(type: KType): Boolean = HttpUrl::class == type.classifier
  override fun safeDecode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<HttpUrl> {
    return StringDecoder().safeDecode(node, type, registry).flatMap {
      it.toHttpUrlOrNull().toOption().fold({ ConfigFailure.DecodeError(node, type).invalid() }, { url -> url.valid() })
    }
  }
}

class HttpUrlBuilderDecoder : NonNullableDecoder<HttpUrl.Builder> {
  override fun supports(type: KType): Boolean = HttpUrl.Builder::class == type.classifier
  override fun safeDecode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<HttpUrl.Builder> {
    return StringDecoder().safeDecode(node, type, registry).flatMap {
      it.toHttpUrlOrNull().toOption().fold({ ConfigFailure.DecodeError(node, type).invalid() },
        { url -> url.newBuilder().valid() })
    }
  }
}
