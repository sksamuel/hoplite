package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.lang.NumberFormatException
import kotlin.reflect.KType

class StringDecoder : NonNullableDecoder<String> {
  override fun supports(type: KType): Boolean = type.classifier == String::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<String> = when (node) {
    is StringValue -> node.value.valid()
    is BooleanValue -> node.value.toString().valid()
    is LongValue -> node.value.toString().valid()
    is DoubleValue -> node.value.toString().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DoubleDecoder : NonNullableDecoder<Double> {
  override fun supports(type: KType): Boolean = type.classifier == Double::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Double> = when (node) {
    is StringValue -> Try { node.value.toDouble() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleValue -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class FloatDecoder : NonNullableDecoder<Float> {
  override fun supports(type: KType): Boolean = type.classifier == Float::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Float> = when (node) {
    is StringValue -> Try { node.value.toFloat() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleValue -> node.value.toFloat().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongDecoder : NonNullableDecoder<Long> {
  override fun supports(type: KType): Boolean = type.classifier == Long::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Long> = when (node) {
    is StringValue -> Try { node.value.toLong() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is LongValue -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class IntDecoder : NonNullableDecoder<Int> {
  override fun supports(type: KType): Boolean = type.classifier == Int::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Int> = when (node) {
    is StringValue -> Try { node.value.toInt() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleValue -> node.value.toInt().valid()
    is LongValue -> node.value.toInt().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ByteDecoder : NonNullableDecoder<Byte> {
  override fun supports(type: KType): Boolean = type.classifier == Byte::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Byte> = when (node) {
    is StringValue -> Try { node.value.toByte() }.toValidated {
      when (it) {
        is NumberFormatException -> ConfigFailure.NumberConversionError(node, type)
        else -> ThrowableFailure(it)
      }
    }
    is DoubleValue -> Try { node.value.toByte() }.toValidated { ThrowableFailure(it) }
    is LongValue -> node.value.toByte().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class ShortDecoder : NonNullableDecoder<Short> {
  override fun supports(type: KType): Boolean = type.classifier == Short::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Short> = when (node) {
    is StringValue -> Try { node.value.toShort() }.toValidated { ThrowableFailure(it) }
    is LongValue -> node.value.toShort().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class BooleanDecoder : NonNullableDecoder<Boolean> {
  override fun supports(type: KType): Boolean = type.classifier == Boolean::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Boolean> = when (node) {
    is StringValue -> when (node.value.toLowerCase()) {
      "true", "t", "1", "yes" -> true.valid()
      "false", "f", "0", "no" -> false.valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
    is BooleanValue -> node.value.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

