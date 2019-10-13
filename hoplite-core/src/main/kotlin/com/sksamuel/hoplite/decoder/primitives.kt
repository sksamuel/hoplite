package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.lang.NumberFormatException
import kotlin.reflect.KType

class StringDecoder : NonNullableDecoder<String> {
  override fun supports(type: KType): Boolean = type.classifier == String::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<String> = when (node) {
    is StringNode -> node.value.valid()
    is BooleanNode -> node.value.toString().valid()
    is LongNode -> node.value.toString().valid()
    is DoubleNode -> node.value.toString().valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DoubleDecoder : NonNullableDecoder<Double> {
  override fun supports(type: KType): Boolean = type.classifier == Double::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Double> = when (node) {
    is StringNode -> Try { node.value.toDouble() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class FloatDecoder : NonNullableDecoder<Float> {
  override fun supports(type: KType): Boolean = type.classifier == Float::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Float> = when (node) {
    is StringNode -> Try { node.value.toFloat() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.toFloat().valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongDecoder : NonNullableDecoder<Long> {
  override fun supports(type: KType): Boolean = type.classifier == Long::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Long> = when (node) {
    is StringNode -> Try { node.value.toLong() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is LongNode -> node.value.valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class IntDecoder : NonNullableDecoder<Int> {
  override fun supports(type: KType): Boolean = type.classifier == Int::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Int> = when (node) {
    is StringNode -> Try { node.value.toInt() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.toInt().valid()
    is LongNode -> node.value.toInt().valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ByteDecoder : NonNullableDecoder<Byte> {
  override fun supports(type: KType): Boolean = type.classifier == Byte::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Byte> = when (node) {
    is StringNode -> Try { node.value.toByte() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> Try { node.value.toByte() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toByte().valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ShortDecoder : NonNullableDecoder<Short> {
  override fun supports(type: KType): Boolean = type.classifier == Short::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Short> = when (node) {
    is StringNode -> Try { node.value.toShort() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toShort().valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class BooleanDecoder : NonNullableDecoder<Boolean> {
  override fun supports(type: KType): Boolean = type.classifier == Boolean::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Boolean> = when (node) {
    is StringNode -> when (node.value.toLowerCase()) {
      "true", "t", "1", "yes" -> true.valid()
      "false", "f", "0", "no" -> false.valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
    is BooleanNode -> node.value.valid()
    is MapNode -> safeDecode(node.value, type, registry)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

