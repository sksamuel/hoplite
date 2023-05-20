package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

class IntRangeDecoder : NullHandlingDecoder<IntRange> {
  override fun supports(type: KType): Boolean = type.classifier == IntRange::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<IntRange> = when (node) {
    is StringNode -> RangeDecoders.intRange(node.value).map { it.valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongRangeDecoder : NullHandlingDecoder<LongRange> {
  override fun supports(type: KType): Boolean = type.classifier == LongRange::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<LongRange> = when (node) {
    is StringNode -> RangeDecoders.longRange(node.value).map { it.valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class CharRangeDecoder : NullHandlingDecoder<CharRange> {
  override fun supports(type: KType): Boolean = type.classifier == CharRange::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<CharRange> = when (node) {
    is StringNode -> RangeDecoders.charRange(node.value).map { it.valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

object RangeDecoders {

  private val numericRangePattern = "(\\d+)\\.\\.(\\d+)".toRegex()

  private val textRangePattern = "(\\w+)\\.\\.(\\w+)".toRegex()

  fun intRange(value: String): Result<IntRange> {
    return extractContents(value, numericRangePattern)
      .flatMap { (first, second) -> runCatching { IntRange(first.toInt(), second.toInt()) } }
  }

  fun longRange(value: String): Result<LongRange> {
    return extractContents(value, numericRangePattern)
      .flatMap { (first, second) -> runCatching { LongRange(first.toLong(), second.toLong()) } }
  }

  fun charRange(value: String): Result<CharRange> {
    return extractContents(value, textRangePattern)
      .flatMap { (first, second) -> runCatching { CharRange(first[0], second[0]) } }
  }

  private fun extractContents(value: String, r: Regex): Result<Pair<String, String>> = runCatching {
      val (_, start, endInclusive) = r.matchEntire(value)?.groupValues.orEmpty()
      Pair(start, endInclusive)
    }

}
