package com.sksamuel.hoplite.converter

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

class StringConverterProvider : ParameterizedConverterProvider<String>() {
  override fun converter(): Converter<String> = object : Converter<String> {
    override fun apply(value: Value): ConfigResult<String> = value.string()
  }
}

class DoubleConverterProvider : ParameterizedConverterProvider<Double>() {
  override fun converter(): Converter<Double> = object : Converter<Double> {
    override fun apply(value: Value): ConfigResult<Double> = when (value) {
      is StringValue -> Try { value.value.toDouble() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> value.value.validNel()
      else -> ConfigFailure.conversionFailure<Double>(value).invalidNel()
    }
  }
}

class FloatConverterProvider : ParameterizedConverterProvider<Float>() {
  override fun converter(): Converter<Float> = object : Converter<Float> {
    override fun apply(value: Value): ConfigResult<Float> = when (value) {
      is StringValue -> Try { value.value.toFloat() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> value.value.toFloat().validNel()
      else -> ConfigFailure.conversionFailure<Float>(value).invalidNel()
    }
  }
}

class LongConverterProvider : ParameterizedConverterProvider<Long>() {
  override fun converter(): Converter<Long> = object : Converter<Long> {
    override fun apply(value: Value): ConfigResult<Long> = when (value) {
      is StringValue -> Try { value.value.toLong() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is LongValue -> value.value.validNel()
      else -> ConfigFailure.conversionFailure<Long>(value).invalidNel()
    }
  }
}

class IntConverterProvider : ParameterizedConverterProvider<Int>() {
  override fun converter(): Converter<Int> = object : Converter<Int> {
    override fun apply(value: Value): ConfigResult<Int> = when (value) {
      is StringValue -> Try { value.value.toInt() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> value.value.toInt().validNel()
      is LongValue -> value.value.toInt().validNel()
      else -> ConfigFailure.conversionFailure<Int>(value).invalidNel()
    }
  }
}

class ByteConverterProvider : ParameterizedConverterProvider<Byte>() {
  override fun converter(): Converter<Byte> = object : Converter<Byte> {
    override fun apply(value: Value): ConfigResult<Byte> = when (value) {
      is StringValue -> Try { value.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> Try { value.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is LongValue -> value.value.toByte().validNel()
      else -> ConfigFailure.conversionFailure<Int>(value).invalidNel()
    }
  }
}

class BooleanConverterProvider : ParameterizedConverterProvider<Boolean>() {
  override fun converter(): Converter<Boolean> = object : Converter<Boolean> {
    override fun apply(value: Value): ConfigResult<Boolean> = when (value) {
      is StringValue -> Try { value.value.toBoolean() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is BooleanValue -> value.value.validNel()
      else -> ConfigFailure.conversionFailure<Int>(value).invalidNel()
    }
  }
}