package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.lang.NumberFormatException
import kotlin.reflect.KType

class StringDecoder : NonNullableDecoder<String> {
  override fun supports(type: KType): Boolean = type.classifier == String::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<String> = when (val v = node.value) {
    is Value.StringNode -> v.value.valid()
    is Value.BooleanNode -> v.value.toString().valid()
    is Value.LongNode -> v.value.toString().valid()
    is Value.DoubleNode -> v.value.toString().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DoubleDecoder : NonNullableDecoder<Double> {
  override fun supports(type: KType): Boolean = type.classifier == Double::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Double> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toDouble() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is Value.DoubleNode -> v.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class FloatDecoder : NonNullableDecoder<Float> {
  override fun supports(type: KType): Boolean = type.classifier == Float::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Float> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toFloat() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is Value.DoubleNode -> v.value.toFloat().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongDecoder : NonNullableDecoder<Long> {
  override fun supports(type: KType): Boolean = type.classifier == Long::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Long> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toLong() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is Value.LongNode -> v.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class IntDecoder : NonNullableDecoder<Int> {
  override fun supports(type: KType): Boolean = type.classifier == Int::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Int> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toInt() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is Value.DoubleNode -> v.value.toInt().valid()
    is Value.LongNode -> v.value.toInt().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ByteDecoder : NonNullableDecoder<Byte> {
  override fun supports(type: KType): Boolean = type.classifier == Byte::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Byte> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toByte() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is Value.DoubleNode -> Try { v.value.toByte() }.toValidated { ThrowableFailure(it) }
    is Value.LongNode -> v.value.toByte().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ShortDecoder : NonNullableDecoder<Short> {
  override fun supports(type: KType): Boolean = type.classifier == Short::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Short> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toShort() }.toValidated { ThrowableFailure(it) }
    is Value.LongNode -> v.value.toShort().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class BooleanDecoder : NonNullableDecoder<Boolean> {
  override fun supports(type: KType): Boolean = type.classifier == Boolean::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Boolean> = when (val v = node.value) {
    is Value.StringNode -> when (v.value.toLowerCase()) {
      "true", "t", "1", "yes" -> true.valid()
      "false", "f", "0", "no" -> false.valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
    is Value.BooleanNode -> v.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

