package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.fp.Try
import java.lang.NumberFormatException
import kotlin.reflect.KType

class StringDecoder : NonNullableLeafDecoder<String> {
  override fun supports(type: KType): Boolean = type.classifier == String::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<String> = when (node) {
    is StringNode -> node.value.valid()
    is BooleanNode -> node.value.toString().valid()
    is LongNode -> node.value.toString().valid()
    is DoubleNode -> node.value.toString().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DoubleDecoder : NonNullableLeafDecoder<Double> {
  override fun supports(type: KType): Boolean = type.classifier == Double::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Double> = when (node) {
    is StringNode -> Try { node.value.toDouble() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class FloatDecoder : NonNullableLeafDecoder<Float> {
  override fun supports(type: KType): Boolean = type.classifier == Float::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Float> = when (node) {
    is StringNode -> Try { node.value.toFloat() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.toFloat().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongDecoder : NonNullableLeafDecoder<Long> {
  override fun supports(type: KType): Boolean = type.classifier == Long::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Long> = when (node) {
    is StringNode -> Try { node.value.toLong() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is LongNode -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class IntDecoder : NonNullableLeafDecoder<Int> {
  override fun supports(type: KType): Boolean = type.classifier == Int::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Int> = when (node) {
    is StringNode -> Try { node.value.toInt() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> node.value.toInt().valid()
    is LongNode -> node.value.toInt().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ByteDecoder : NonNullableLeafDecoder<Byte> {
  override fun supports(type: KType): Boolean = type.classifier == Byte::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Byte> = when (node) {
    is StringNode -> Try { node.value.toByte() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleNode -> Try { node.value.toInt().toByte() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toByte().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ShortDecoder : NonNullableLeafDecoder<Short> {
  override fun supports(type: KType): Boolean = type.classifier == Short::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Short> = when (node) {
    is StringNode -> Try { node.value.toShort() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toShort().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class BooleanDecoder : NonNullableLeafDecoder<Boolean> {
  override fun supports(type: KType): Boolean = type.classifier == Boolean::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Boolean> = when (node) {
    is StringNode -> when (node.value.toLowerCase()) {
      "true", "t", "1", "yes" -> true.valid()
      "false", "f", "0", "no" -> false.valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
    is BooleanNode -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

