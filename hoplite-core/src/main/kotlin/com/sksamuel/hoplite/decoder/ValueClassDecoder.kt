package com.sksamuel.hoplite.decoder

import arrow.core.invalid
import arrow.core.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

interface ValueType

class ValueClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean =
    type.classifier is KClass<*> &&
      (type.classifier as KClass<*>).isData &&
      (type.classifier as KClass<*>).primaryConstructor?.parameters?.size == 1 &&
      (type.isSubtypeOf(ValueType::class.createType()))

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Any> {

    val constructor = (type.classifier as KClass<*>).primaryConstructor?.valid()
      ?: ConfigFailure.MissingPrimaryConstructor(type).invalid()

    return constructor.flatMap { constr ->

      val param = constr.parameters[0]

      if (node is Undefined) {
        ConfigFailure.NullValueForNonNullField(node).invalid()
      } else {
        context.decoder(param)
          .flatMap { it.decode(node, param.type, context) }
          .leftMap { ConfigFailure.ValueTypeIncompatible(param.type, node) }
          .map { constr.call(it) }
      }
    }
  }
}
