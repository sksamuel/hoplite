package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
class EnumDecoder<T : Any> : NonNullableDecoder<T> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).java.isEnum

  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<T> {

    val klass = type.classifier as KClass<*>

    fun decode(value: String): ConfigResult<T> {
      val t = klass.java.enumConstants.find { it.toString() == value }
      return if (t == null)
        ConfigFailure.InvalidEnumConstant(node, type, value).invalid()
      else
        (t as T).valid()
    }

    return when (node) {
      is StringValue -> decode(node.value)
      is BooleanValue -> decode(node.value.toString())
      is LongValue -> decode(node.value.toString())
      is DoubleValue -> decode(node.value.toString())
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
