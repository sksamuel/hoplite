package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.valid
import arrow.data.validNel
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigResults
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.toValidated
import kotlin.reflect.KType

class StringDecoder : BasicDecoder<String> {
  override fun supports(type: KType): Boolean = type.classifier == String::class
  override fun decode(node: Node, path: String): ConfigResult<String> = when (node) {
    is StringNode -> node.value.valid()
    is BooleanNode -> node.value.toString().valid()
    is LongNode -> node.value.toString().valid()
    is DoubleNode -> node.value.toString().valid()
    else -> ConfigResults.decodeFailure(node, path, String::class)
  }
}

class DoubleDecoder : BasicDecoder<Double> {
  override fun supports(type: KType): Boolean = type.classifier == Double::class
  override fun decode(node: Node, path: String): ConfigResult<Double> = when (node) {
    is StringNode -> Try { node.value.toDouble() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is DoubleNode -> node.value.validNel()
    else -> ConfigFailure.conversionFailure<Double>(node).invalidNel()
  }
}

class FloatDecoder : BasicDecoder<Float> {
  override fun supports(type: KType): Boolean = type.classifier == Float::class
  override fun decode(node: Node, path: String): ConfigResult<Float> = when (node) {
    is StringNode -> Try { node.value.toFloat() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is DoubleNode -> node.value.toFloat().validNel()
    else -> ConfigFailure.conversionFailure<Float>(node).invalidNel()
  }
}

class LongDecoder : BasicDecoder<Long> {
  override fun supports(type: KType): Boolean = type.classifier == Long::class
  override fun decode(node: Node, path: String): ConfigResult<Long> = when (node) {
    is StringNode -> Try { node.value.toLong() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is LongNode -> node.value.validNel()
    else -> ConfigFailure.conversionFailure<Long>(node).invalidNel()
  }
}

class IntDecoder : BasicDecoder<Int> {
  override fun supports(type: KType): Boolean = type.classifier == Int::class
  override fun decode(node: Node, path: String): ConfigResult<Int> = when (node) {
    is StringNode -> Try { node.value.toInt() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is DoubleNode -> node.value.toInt().validNel()
    is LongNode -> node.value.toInt().validNel()
    else -> ConfigFailure.conversionFailure<Int>(node).invalidNel()
  }
}

class ByteDecoder : BasicDecoder<Byte> {
  override fun supports(type: KType): Boolean = type.classifier == Byte::class
  override fun decode(node: Node, path: String): ConfigResult<Byte> = when (node) {
    is StringNode -> Try { node.value.toByte() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is DoubleNode -> Try { node.value.toByte() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is LongNode -> node.value.toByte().validNel()
    else -> ConfigFailure.conversionFailure<Byte>(node).invalidNel()
  }
}

class ShortDecoder : BasicDecoder<Short> {
  override fun supports(type: KType): Boolean = type.classifier == Short::class
  override fun decode(node: Node, path: String): ConfigResult<Short> = when (node) {
    is StringNode -> Try { node.value.toShort() }.toValidated { ThrowableFailure(it) }.toValidatedNel()
    is LongNode -> node.value.toShort().validNel()
    else -> ConfigFailure.conversionFailure<Short>(node).invalidNel()
  }
}

class BooleanDecoder : BasicDecoder<Boolean> {
  override fun supports(type: KType): Boolean = type.classifier == Boolean::class
  override fun decode(node: Node, path: String): ConfigResult<Boolean> = when (node) {
    is StringNode -> when (node.value.toLowerCase()) {
      "true", "t", "1", "yes" -> true.validNel()
      "false", "f", "0", "no" -> false.validNel()
      else -> ConfigResults.decodeFailure(node, path, Boolean::class)
    }
    is BooleanNode -> node.value.validNel()
    else -> ConfigFailure.conversionFailure<Boolean>(node).invalidNel()
  }
}

