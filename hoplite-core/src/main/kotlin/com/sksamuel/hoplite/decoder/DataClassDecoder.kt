package com.sksamuel.hoplite.decoder

import arrow.core.ValidatedNel
import arrow.core.handleLeftWith
import arrow.core.invalid
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
import kotlin.reflect.full.createType

class SealedClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isSealed

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Any> {
    // to determine which sealed class to use, we can just try each in turn until one results in success
    val kclass = type.classifier as KClass<*>
    val subclasses = kclass.sealedSubclasses
    return if (subclasses.isEmpty()) ConfigFailure.SealedClassWithoutImpls(kclass).invalid() else {
      val error = ConfigFailure.NoSealedClassSubtype(kclass, node).invalid()
      kclass.sealedSubclasses.fold<KClass<*>, ConfigResult<Any>>(error) { acc, subclass ->
        acc.handleLeftWith {
          val decoded = DataClassDecoder().decode(node, subclass.createType(), context)
          if (decoded.isInvalid) acc else decoded
        }
      }
    }
  }
}

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>
    val constructor = klass.constructors.first()

    val args: ValidatedNel<ConfigFailure.ParamFailure, List<Pair<KParameter, Any?>>> = constructor.parameters.mapNotNull { param ->

      val name = param.name ?: "<anon>"
      val n = context.paramMappers.fold<ParameterMapper, Node>(Undefined) { n, mapper ->
        if (n.isDefined) n else node.atKey(mapper.name(name))
      }

      if (param.isOptional && n is Undefined) {
        null // skip this parameter and let the default value be filled in
      } else {
        context.decoder(param)
          .flatMap { it.decode(n, param.type, context) }
          .map { param to it }
          .leftMap { ConfigFailure.ParamFailure(name, it) }
      }
    }.sequence()

    return args
      .leftMap { ConfigFailure.DataClassFieldErrors(it, type, node.pos) }
      .map { constructor.callBy(it.toMap()) }
  }
}
