package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
class EnumDecoder<T : Any>(private val ignoreCase: Boolean = false) : NullHandlingDecoder<T> {

  override fun priority(): Int = super.priority().takeUnless { ignoreCase } ?: 0

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).java.isEnum

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext
  ): ConfigResult<T> {

    val klass = type.classifier as KClass<*>

    fun decode(value: String): ConfigResult<T> {
      val t = klass.java.enumConstants.find { it.toString().contentEquals(value, ignoreCase) }
      return if (t == null)
        ConfigFailure.InvalidEnumConstant(node, type, value).invalid()
      else
        (t as T).valid()
    }

    return when (node) {
      is StringNode -> decode(node.value)
      is BooleanNode -> decode(node.value.toString())
      is LongNode -> decode(node.value.toString())
      is DoubleNode -> decode(node.value.toString())
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
