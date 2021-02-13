package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.preprocessor.Preprocessor
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Contains the configuration needed for decoders to work. For example, the context allows access to the
 * [DecoderRegistry] using which decoders can lookup other decoders to be used for nested types.
 */
data class DecoderContext(val decoders: DecoderRegistry,
                          val paramMappers: List<ParameterMapper>,
                          val preprocessors: List<Preprocessor>,
                          val mode: DecodeMode = DecodeMode.Lenient) {
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type.type)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), emptyList(), DecodeMode.Lenient)
    operator fun invoke(registry: DecoderRegistry) = DecoderContext(registry, emptyList(), emptyList(), DecodeMode.Lenient)
  }
}

enum class DecodeMode {
  Strict, Lenient
}
