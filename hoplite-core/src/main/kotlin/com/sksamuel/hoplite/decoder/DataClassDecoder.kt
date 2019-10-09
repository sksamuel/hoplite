package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.UndefinedValue
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(value: Value,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>
    val constructor = klass.constructors.first()

    val args: ValidatedNel<ConfigFailure.ParamFailure, List<Pair<KParameter, Any?>>> = constructor.parameters.mapNotNull { param ->
      val paramName = param.name ?: "<anon>"
      val n = value.atKey(paramName)

      if (param.isOptional && n is UndefinedValue) {
        null // skip this parameter and let the default value be filled in
      } else {
        registry.decoder(param.type)
          .flatMap { it.decode(n, param.type, registry) }
          .map { param to it }
          .leftMap { ConfigFailure.ParamFailure(paramName, it) }
      }
    }.sequence()

    return args
      .leftMap { ConfigFailure.DataClassFieldErrors(it, type, value.pos) }
      .map { constructor.callBy(it.toMap()) }
  }
}
