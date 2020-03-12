package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

private typealias Rule = (String) -> String

// Redundant escaping in this file required for Android support.

object RandomPreprocessor : StringNodePreprocessor() {

  private const val a = 33 // '!'
  private const val z = 126 // '~'

  private val oldRule: Rule = {
    val regex = "\\\$RANDOM_STRING\\((\\d+)\\)".toRegex()
    regex.replace(it) { match ->
      val length = match.groupValues[1].toInt()
      val chars = CharArray(length) { Random.nextInt(a, z).toChar() }
      String(chars)
    }
  }

  private val intRule: Rule = {
    val regex = "\\$\\{random.int\\}".toRegex()
    regex.replace(it) { abs(Random.nextInt()).toString() }
  }

  private val booleanRule: Rule = {
    val regex = "\\$\\{random.boolean\\}".toRegex()
    regex.replace(it) { Random.nextBoolean().toString() }
  }

  private val intWithMaxRule: Rule = {
    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val max = match.groupValues[1].toInt()
      Random.nextInt(0, max).toString()
    }
  }

  private val intWithRangeRule: Rule = {
    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val min = match.groupValues[1].toInt()
      val max = match.groupValues[2].toInt()
      Random.nextInt(min, max).toString()
    }
  }

  private val longRule: Rule = {
    val regex = "\\$\\{random.long\\}".toRegex()
    regex.replace(it) { abs(Random.nextLong()).toString() }
  }

  private val doubleRule: Rule = {
    val regex = "\\$\\{random.double\\}".toRegex()
    regex.replace(it) { Random.nextLong().toString() }
  }

  private val stringRule: Rule = {
    val regex = "\\$\\{random.string\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
    regex.replace(it) { match ->
      val length = match.groupValues[1].toInt()
      val chars = CharArray(length) { Random.nextInt(a, z).toChar() }
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
    oldRule,
    intRule,
    longRule,
    intWithMaxRule,
    booleanRule,
    doubleRule,
    intWithRangeRule,
    stringRule,
    uuidRule
  )

  override fun map(node: StringNode): Node {
    val value = rules.fold(node.value) { str, rule -> rule(str) }
    return node.copy(value = value)
  }
}
