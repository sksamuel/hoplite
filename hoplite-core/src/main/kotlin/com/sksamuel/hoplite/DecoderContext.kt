package com.sksamuel.hoplite

import arrow.core.Validated
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import kotlin.reflect.KParameter
import kotlin.reflect.KType

data class DecoderContext(val decoders: DecoderRegistry,
                          val paramMappers: List<ParameterMapper>) {
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type.type)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList())
  }
}
