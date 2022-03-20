package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.preprocessor.Preprocessor
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Contains the configuration needed for decoders to work. For example, the context allows access to the
 * [DecoderRegistry] through which decoders can look up other decoders to be used for nested types.
 */
data class DecoderContext(
  val decoders: DecoderRegistry,
  val paramMappers: List<ParameterMapper>,
  val preprocessors: List<Preprocessor>,
  val usedPaths: MutableSet<DotPath> = mutableSetOf(),
  val secrets: MutableSet<DotPath> = mutableSetOf(),
) {

  /**
   * Returns a [Decoder] for type [type].
   */
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)

  /**
   * Returns a [Decoder] for type [KParameter].
   */
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoder(type.type)

  companion object {

    val zero = DecoderContext(DecoderRegistry.zero, emptyList(), emptyList(), mutableSetOf())

    operator fun invoke(registry: DecoderRegistry) =
      DecoderContext(registry, emptyList(), emptyList(), mutableSetOf())
  }
}

enum class DecodeMode {
  Strict, Lenient
}
