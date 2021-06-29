package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.flatMapInvalid
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.primaryConstructor

class SealedClassDecoder : NullHandlingDecoder<Any> {

  override fun supports(type: KType): Boolean {
    return when (type.classifier is KClass<*>) {
      true -> {
        val kclass = type.classifier as KClass<*>
        kclass.isSealed
      }
      false -> false
    }
  }

  // it's common to have custom decoders for sealed classes, like options etc, so sealed classes should be very low priority
  override fun priority(): Int = Integer.MIN_VALUE + 100

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Any> {
    // to determine which sealed class to use, we can just try each in turn until one results in success
    val kclass = type.classifier as KClass<*>
    val subclasses = kclass.sealedSubclasses
    return if (subclasses.isEmpty()) ConfigFailure.SealedClassWithoutImpls(kclass).invalid() else {
      val error = ConfigFailure.NoSealedClassSubtype(kclass, node).invalid()
      kclass.sealedSubclasses
        .sortedBy { it.primaryConstructor?.parameters?.size ?: 0 }
        .reversed()
        .fold<KClass<*>, ConfigResult<Any>>(error) { acc, subclass ->
          acc.flatMapInvalid {
            val decoded = DataClassDecoder().decode(node, subclass.createType(), context)
            if (decoded.isInvalid()) acc else decoded
          }
        }
    }
  }
}
