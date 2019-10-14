package com.sksamuel.hoplite.decoder

import arrow.core.handleLeftWith
import arrow.core.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import kotlin.reflect.KClass
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
