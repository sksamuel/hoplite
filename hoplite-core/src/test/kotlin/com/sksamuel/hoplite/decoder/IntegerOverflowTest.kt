package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderConfig
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.defaultNodeTransformers
import com.sksamuel.hoplite.defaultParamMappers
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.full.createType

class IntegerOverflowTest : FunSpec({

  // Regression: LongNode -> IntDecoder used Long.toInt(), which silently truncates a Long
  // outside Int range. JSON/YAML/HOCON can produce a LongNode for any integer literal that
  // doesn't fit in 32 bits, so this path is reachable in real configs (a numeric literal of
  // 3_000_000_000 would arrive as a LongNode and decode to a negative Int). The String path
  // already threw NumberFormatException for the same input — so the LongNode path was the
  // only one that was lossy.
  fun ctx() = DecoderContext(
    decoders = defaultDecoderRegistry(),
    paramMappers = defaultParamMappers(),
    nodeTransformers = defaultNodeTransformers(),
    config = DecoderConfig(flattenArraysToString = false, resolveTypesCaseInsensitive = false),
  )

  test("IntDecoder on a LongNode larger than Int.MAX_VALUE returns Invalid, not a truncated Int") {
    val node = LongNode(3_000_000_000L, Pos.NoPos, DotPath.root)
    val result = IntDecoder().decode(node, Int::class.createType(), ctx())
    result.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
  }

  test("IntDecoder on a LongNode smaller than Int.MIN_VALUE returns Invalid, not a truncated Int") {
    val node = LongNode(-3_000_000_000L, Pos.NoPos, DotPath.root)
    val result = IntDecoder().decode(node, Int::class.createType(), ctx())
    result.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
  }

  test("ShortDecoder on a LongNode larger than Short.MAX_VALUE returns Invalid, not a truncated Short") {
    val node = LongNode(100_000L, Pos.NoPos, DotPath.root)
    val result = ShortDecoder().decode(node, Short::class.createType(), ctx())
    result.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
  }

  test("ByteDecoder on a LongNode larger than Byte.MAX_VALUE returns Invalid, not a truncated Byte") {
    val node = LongNode(1000L, Pos.NoPos, DotPath.root)
    val result = ByteDecoder().decode(node, Byte::class.createType(), ctx())
    result.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
  }

  test("IntDecoder on a LongNode that does fit decodes successfully") {
    val node = LongNode(42L, Pos.NoPos, DotPath.root)
    val result = IntDecoder().decode(node, Int::class.createType(), ctx())
    result.shouldBeInstanceOf<Validated.Valid<Int>>()
  }
})
