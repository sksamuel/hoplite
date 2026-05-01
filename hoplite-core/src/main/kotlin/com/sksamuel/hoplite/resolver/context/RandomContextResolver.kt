package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

object RandomContextResolver : ContextResolver() {

  private val a = 'a'
  private val z = 'z'

  override val contextKey: String = "random"
  override val default: Boolean = false

  // The path passed to lookup() is just the inside of `${{ random:... }}` — e.g. `int(10)` —
  // not the surrounding `${random.int(10)}` syntax. The previous regexes were anchored to the
  // outer form, so they could never match and `${{ random:int(N) }}` and friends fell through to
  // null, leaving the placeholder unresolved.
  private val intWithMaxRule = "int\\(\\s*(\\d+)\\s*\\)".toRegex()
  private val intWithRangeRule = "int\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)".toRegex()
  private val stringRule = "string\\(\\s*(\\d+)\\s*\\)".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return when (path) {
      "int" -> abs(Random.nextInt()).toString().valid()
      "boolean" -> Random.nextBoolean().toString().valid()
      "long" -> abs(Random.nextLong()).toString().valid()
      "double" -> abs(Random.nextDouble()).toString().valid()
      "uuid" -> UUID.randomUUID().toString().valid()
      else -> {
        val intWithMaxMatch = intWithMaxRule.matchEntire(path)
        val intWithRangeMatch = intWithRangeRule.matchEntire(path)
        val stringMatch = stringRule.matchEntire(path)
        when {
          intWithMaxMatch != null -> {
            val max = intWithMaxMatch.groupValues[1].toInt()
            Random.nextInt(0, max).toString().valid()
          }
          intWithRangeMatch != null -> {
            val min = intWithRangeMatch.groupValues[1].toInt()
            val max = intWithRangeMatch.groupValues[2].toInt()
            Random.nextInt(min, max).toString().valid()
          }
          stringMatch != null -> {
            val length = stringMatch.groupValues[1].toInt()
            val chars = CharArray(length) { Random.nextInt(a.code, z.code).toChar() }
            String(chars).valid()
          }
          else -> null.valid()
        }
      }
    }
  }
}
