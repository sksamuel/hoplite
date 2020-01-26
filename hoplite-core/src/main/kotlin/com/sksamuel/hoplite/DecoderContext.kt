package com.sksamuel.hoplite

import arrow.core.Validated
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Contains the configuration needed for decoders to work. For example, the context allows access to the
 * [DecoderRegistry] using which decoders can lookup other decoders to be used for nested types.
 */
data class DecoderContext(val decoders: DecoderRegistry,
                          val paramMappers: List<ParameterMapper>,
                          val preprocessors: List<Preprocessor>) {
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type.type)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), emptyList())
  }
}
