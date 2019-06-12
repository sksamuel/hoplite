package com.sksamuel.hoplite.converter

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated

inline fun <reified T> basicConverter(): Converter<T> = object : Converter<T> {
  override fun apply(cursor: Cursor): ConfigResult<T> {
    // println("Is ${cursor.value()} a ${T::class}" + (cursor.value() is T))
    return when (val v = cursor.value()) {
      is T -> v.validNel()
      else -> ConfigFailure.conversionFailure<T>(v).invalidNel()
    }
  }
}

class StringConverterProvider : ParameterizedConverterProvider<String>() {
  override fun converter(): Converter<String> = basicConverter()
}

class DoubleConverterProvider : ParameterizedConverterProvider<Double>() {
  override fun converter(): Converter<Double> = object : Converter<Double> {
    override fun apply(cursor: Cursor): ConfigResult<Double> = when (val v = cursor.value()) {
      is String -> Try { v.toDouble() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is Float -> v.toDouble().validNel()
      is Double -> v.validNel()
      else -> ConfigFailure.conversionFailure<Double>(v).invalidNel()
    }
  }
}

class FloatConverterProvider : ParameterizedConverterProvider<Float>() {
  override fun converter(): Converter<Float> = object : Converter<Float> {
    override fun apply(cursor: Cursor): ConfigResult<Float> = when (val v = cursor.value()) {
      is String -> Try { v.toFloat() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is Float -> v.validNel()
      is Double -> v.toFloat().validNel()
      else -> ConfigFailure.conversionFailure<Float>(v).invalidNel()
    }
  }
}

class LongConverterProvider : ParameterizedConverterProvider<Long>() {
  override fun converter(): Converter<Long> = object : Converter<Long> {
    override fun apply(cursor: Cursor): ConfigResult<Long> = when (val v = cursor.value()) {
      is String -> Try { v.toLong() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is Int -> v.toLong().validNel()
      is Long -> v.validNel()
      else -> ConfigFailure.conversionFailure<Long>(v).invalidNel()
    }
  }
}

class IntConverterProvider : ParameterizedConverterProvider<Int>() {
  override fun converter(): Converter<Int> = object : Converter<Int> {
    override fun apply(cursor: Cursor): ConfigResult<Int> = when (val v = cursor.value()) {
      is String -> Try { v.toInt() }.toValidated { ThrowableFailure(it, null) }.toValidatedNel()
      is Int -> v.validNel()
      is Long -> v.toInt().validNel()
      else -> ConfigFailure.conversionFailure<Int>(v).invalidNel()
    }
  }
}

class BooleanConverterProvider : ParameterizedConverterProvider<Boolean>() {
  override fun converter(): Converter<Boolean> = basicConverter()
}