package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import kotlin.reflect.KType

/**
 * Contains the configuration needed for decoders to work. For example, the context allows access to the
 * [DecoderRegistry] using which decoders can lookup other decoders to be used for nested types.
 */
data class DecoderContext(
  val decoders: DecoderRegistry,
  val paramMappers: List<ParameterMapper>,
  val preprocessors: List<Preprocessor>
) {

  /**
   * Returns the highest priority [Decoder] registered for the given [KType].
   */
  fun decoder(type: KType): ConfigResult<Decoder<*>> = decoders.decoder(type)
//  fun decoder(type: KParameter): ConfigResult<Decoder<*>> = decoders.decoder(type.type)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), emptyList())
    operator fun invoke(registry: DecoderRegistry) = DecoderContext(registry, emptyList(), emptyList())
  }
}
