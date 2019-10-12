package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
class EnumDecoder<T : Any> : NonNullableDecoder<T> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).java.isEnum

  override fun safeDecode(node: TreeNode,
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

    return when (val v = node.value) {
      is Value.StringNode -> decode(v.value)
      is Value.BooleanNode -> decode(v.value.toString())
      is Value.LongNode -> decode(v.value.toString())
      is Value.DoubleNode -> decode(v.value.toString())
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
