package com.sksamuel.hoplite.decoder

import arrow.core.ValidatedNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ParameterMapper
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.isDefined
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>
    val constructor = klass.constructors.first()

    val args: ValidatedNel<ConfigFailure.ParamFailure, List<Pair<KParameter, Any?>>> = constructor.parameters.mapNotNull { param ->

      val n = context.paramMappers.fold<ParameterMapper, Node>(Undefined) { n, mapper ->
        if (n.isDefined) n else node.atKey(mapper.map(param))
      }

      val processed = context.preprocessors.fold(n) { acc, pp -> pp.process(acc) }

      if (param.isOptional && processed is Undefined) {
        null // skip this parameter and let the default value be filled in
      } else {
        context.decoder(param)
          .flatMap { it.decode(processed, param.type, context) }
          .map { param to it }
          .leftMap { ConfigFailure.ParamFailure(param, it) }
      }
    }.sequence()

    return args
      .leftMap { ConfigFailure.DataClassFieldErrors(it, type, node.pos) }
      .map { constructor.callBy(it.toMap()) }
  }
}
