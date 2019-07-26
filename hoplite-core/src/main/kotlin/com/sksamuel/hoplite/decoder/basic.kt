package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import kotlin.reflect.KType

class BasicDecoderFactory : DecoderFactory {
  override fun build(type: KType, registry: DecoderRegistry): Decoder<*>? = when {
    type.classifier == String::class -> StringDecoder
    type.classifier == Double::class -> DoubleDecoder
    type.classifier == Float::class -> FloatDecoder
    type.classifier == Boolean::class -> BooleanDecoder
    type.classifier == Int::class -> IntDecoder
    type.classifier == Long::class -> LongDecoder
    type.classifier == Byte::class -> ByteDecoder
    type.classifier == Short::class -> ShortDecoder
    else -> null
  }
}

object StringDecoder : Decoder<String> {
  override fun convert(value: Value): ConfigResult<String> = value.string()
}

object DoubleDecoder : Decoder<Double> {
  override fun convert(value: Value): ConfigResult<Double> = when (value) {
    is StringValue -> Try { value.value.toDouble() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is DoubleValue -> value.value.validNel()
    else -> ConfigFailure.conversionFailure<Double>(value).invalidNel()
  }
}

object FloatDecoder : Decoder<Float> {
  override fun convert(value: Value): ConfigResult<Float> = when (value) {
    is StringValue -> Try { value.value.toFloat() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is DoubleValue -> value.value.toFloat().validNel()
    else -> ConfigFailure.conversionFailure<Float>(value).invalidNel()
  }
}

object LongDecoder : Decoder<Long> {
  override fun convert(value: Value): ConfigResult<Long> = when (value) {
    is StringValue -> Try { value.value.toLong() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is LongValue -> value.value.validNel()
    else -> ConfigFailure.conversionFailure<Long>(value).invalidNel()
  }
}

object IntDecoder : Decoder<Int> {
  override fun convert(value: Value): ConfigResult<Int> = when (value) {
    is StringValue -> Try { value.value.toInt() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is DoubleValue -> value.value.toInt().validNel()
    is LongValue -> value.value.toInt().validNel()
    else -> ConfigFailure.conversionFailure<Int>(value).invalidNel()
  }
}

object ByteDecoder : Decoder<Byte> {
  override fun convert(value: Value): ConfigResult<Byte> = when (value) {
    is StringValue -> Try { value.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is DoubleValue -> Try { value.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is LongValue -> value.value.toByte().validNel()
    else -> ConfigFailure.conversionFailure<Byte>(value).invalidNel()
  }
}

object ShortDecoder : Decoder<Short> {
  override fun convert(value: Value): ConfigResult<Short> = when (value) {
    is StringValue -> Try { value.value.toShort() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is LongValue -> value.value.toShort().validNel()
    else -> ConfigFailure.conversionFailure<Short>(value).invalidNel()
  }
}

object BooleanDecoder : Decoder<Boolean> {
  override fun convert(value: Value): ConfigResult<Boolean> = when (value) {
    is StringValue -> Try { value.value.toBoolean() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
    is BooleanValue -> value.value.validNel()
    else -> ConfigFailure.conversionFailure<Boolean>(value).invalidNel()
  }
}