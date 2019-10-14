package com.sksamuel.hoplite

import arrow.core.Validated
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  SnakeCaseParamMapper,
  KebabCaseParamMapper
)

interface ParameterMapper {
  fun name(param: KParameter): String = name(param.name ?: "<anon>")
  fun name(param: KProperty1<*, *>): String = name(param.name)
  fun name(name: String): String
}

object DefaultParamMapper : ParameterMapper {
  override fun name(name: String): String = name
}

data class DecoderContext(val decoders: DecoderRegistry,
                          val paramMappers: List<ParameterMapper>) {
  fun decoder(type: KType): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type)
  fun decoder(type: KParameter): Validated<ConfigFailure, Decoder<*>> = decoders.decoder(type.type)

  companion object {
    val zero = DecoderContext(DecoderRegistry.zero, emptyList())
  }
}
