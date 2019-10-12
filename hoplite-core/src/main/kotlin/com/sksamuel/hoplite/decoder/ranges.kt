package com.sksamuel.hoplite.decoder

import kotlin.reflect.KType

import arrow.core.Try
import arrow.core.getOrElse
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value

class IntRangeDecoder : NonNullableDecoder<IntRange> {
  override fun supports(type: KType): Boolean = type.classifier == IntRange::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<IntRange> = when (val v = node.value) {
    is Value.StringNode -> RangeDecoders.intRange(v.value).map { it.valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LongRangeDecoder : NonNullableDecoder<LongRange> {
  override fun supports(type: KType): Boolean = type.classifier == LongRange::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<LongRange> = when (val v = node.value) {
    is Value.StringNode -> RangeDecoders.longRange(v.value).map { it.valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class CharRangeDecoder : NonNullableDecoder<CharRange> {
  override fun supports(type: KType): Boolean = type.classifier == CharRange::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<CharRange> = when (val v = node.value) {
    is Value.StringNode -> RangeDecoders.charRange(v.value).map { it.valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

object RangeDecoders {

  private val numericRangePattern = "(\\d+)\\.\\.(\\d+)".toRegex()

  private val textRangePattern = "(\\w+)\\.\\.(\\w+)".toRegex()

  fun intRange(value: String): Try<IntRange> {
    return extractContents(value, numericRangePattern)
      .flatMap { (first, second) -> Try { IntRange(first.toInt(), second.toInt()) } }
  }

  fun longRange(value: String): Try<LongRange> {
    return extractContents(value, numericRangePattern)
      .flatMap { (first, second) -> Try { LongRange(first.toLong(), second.toLong()) } }
  }

  fun charRange(value: String): Try<CharRange> {
    return extractContents(value, textRangePattern)
      .flatMap { (first, second) -> Try { CharRange(first[0], second[0]) } }
  }

  private fun extractContents(value: String, r: Regex): Try<Pair<String, String>> = Try {
      val (_, start, endInclusive) = r.matchEntire(value)?.groupValues ?: emptyList()
      Pair(start, endInclusive)
    }

}
