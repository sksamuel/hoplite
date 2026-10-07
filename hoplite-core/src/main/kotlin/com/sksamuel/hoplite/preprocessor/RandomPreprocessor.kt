// Redundant escaping in this file required for Android support.
@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.DecoderContext
import java.util.UUID
import kotlin.random.Random

private typealias Rule = (String) -> String

object RandomPreprocessor : TraversingPrimitivePreprocessor() {

  private const val a = 'a'
  private const val z = 'z'

  private val intRule: Rule = {
    val regex = "\\$\\{random.int\\}".toRegex()
    // Mask off the sign bit rather than abs(): abs(Int.MIN_VALUE) == Int.MIN_VALUE (overflow).
    regex.replace(it) { (Random.nextInt() and Int.MAX_VALUE).toString() }
  }

  private val booleanRule: Rule = {
    val regex = "\\$\\{random.boolean\\}".toRegex()
    regex.replace(it) { Random.nextBoolean().toString() }
  }

  private val intWithMaxRule: Rule = {
    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val max = match.groupValues[1].toInt()
      // Random.nextInt(0, max) throws IllegalArgumentException when max <= 0.
      // The regex permits "0", so `${random.int(0)}` would propagate that exception
      // out of the preprocessor and break the loader. Leave the placeholder verbatim
      // instead so the user can spot the typo in the report rather than getting a
      // crash deep inside config loading.
      if (max <= 0) match.value else Random.nextInt(0, max).toString()
    }
  }

  private val intWithRangeRule: Rule = {
    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val min = match.groupValues[1].toInt()
      val max = match.groupValues[2].toInt()
      // Same hardening as intWithMaxRule — Random.nextInt(min, max) throws when min >= max.
      if (min >= max) match.value else Random.nextInt(min, max).toString()
    }
  }

  private val longRule: Rule = {
    val regex = "\\$\\{random.long\\}".toRegex()
    // Mask off the sign bit rather than abs(): abs(Long.MIN_VALUE) == Long.MIN_VALUE (overflow).
    regex.replace(it) { (Random.nextLong() and Long.MAX_VALUE).toString() }
  }

  private val doubleRule: Rule = {
    val regex = "\\$\\{random.double\\}".toRegex()
    regex.replace(it) { Random.nextDouble().toString() }
  }

  private val stringRule: Rule = {
    val regex = "\\$\\{random.string\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val length = match.groupValues[1].toInt()
      // Random.nextInt(from, until) is exclusive on `until`, so `nextInt(a.code, z.code)` only
      // produces 'a'..'y' and silently never picks 'z'. Add 1 to include the upper bound.
      val chars = CharArray(length) { Random.nextInt(a.code, z.code + 1).toChar() }
      String(chars)
    }
  }

  private val uuidRule: Rule = {
    val regex = "\\$\\{random.uuid\\}".toRegex()
    regex.replace(it) {
      UUID.randomUUID().toString()
    }
  }

  private val rules = listOf(
    intRule,
    longRule,
    intWithMaxRule,
    booleanRule,
    doubleRule,
    intWithRangeRule,
    stringRule,
    uuidRule
  )

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      val value = rules.fold(node.value) { str, rule -> rule(str) }
      node.copy(value = value).valid()
    }
    else -> node.valid()
  }
}
