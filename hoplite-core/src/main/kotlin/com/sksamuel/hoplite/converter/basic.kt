package com.sksamuel.hoplite.converter

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated

class StringConverterProvider : ParameterizedConverterProvider<String>() {
  override fun converter(): Converter<String> = object : Converter<String> {
    override fun apply(cursor: Cursor): ConfigResult<String> = cursor.asString()
  }
}

class DoubleConverterProvider : ParameterizedConverterProvider<Double>() {
  override fun converter(): Converter<Double> = object : Converter<Double> {
    override fun apply(cursor: Cursor): ConfigResult<Double> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toDouble() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> v.value.validNel()
      else -> ConfigFailure.conversionFailure<Double>(v).invalidNel()
    }
  }
}

class FloatConverterProvider : ParameterizedConverterProvider<Float>() {
  override fun converter(): Converter<Float> = object : Converter<Float> {
    override fun apply(cursor: Cursor): ConfigResult<Float> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toFloat() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> v.value.toFloat().validNel()
      else -> ConfigFailure.conversionFailure<Float>(v).invalidNel()
    }
  }
}

class LongConverterProvider : ParameterizedConverterProvider<Long>() {
  override fun converter(): Converter<Long> = object : Converter<Long> {
    override fun apply(cursor: Cursor): ConfigResult<Long> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toLong() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is LongValue -> v.value.validNel()
      else -> ConfigFailure.conversionFailure<Long>(v).invalidNel()
    }
  }
}

class IntConverterProvider : ParameterizedConverterProvider<Int>() {
  override fun converter(): Converter<Int> = object : Converter<Int> {
    override fun apply(cursor: Cursor): ConfigResult<Int> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toInt() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> v.value.toInt().validNel()
      is LongValue -> v.value.toInt().validNel()
      else -> ConfigFailure.conversionFailure<Int>(v).invalidNel()
    }
  }
}

class ByteConverterProvider : ParameterizedConverterProvider<Byte>() {
  override fun converter(): Converter<Byte> = object : Converter<Byte> {
    override fun apply(cursor: Cursor): ConfigResult<Byte> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is DoubleValue -> Try { v.value.toByte() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is LongValue -> v.value.toByte().validNel()
      else -> ConfigFailure.conversionFailure<Int>(v).invalidNel()
    }
  }
}

class BooleanConverterProvider : ParameterizedConverterProvider<Boolean>() {
  override fun converter(): Converter<Boolean> = object : Converter<Boolean> {
    override fun apply(cursor: Cursor): ConfigResult<Boolean> = when (val v = cursor.value()) {
      is StringValue -> Try { v.value.toBoolean() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is BooleanValue -> v.value.validNel()
      else -> ConfigFailure.conversionFailure<Int>(v).invalidNel()
    }
  }
}