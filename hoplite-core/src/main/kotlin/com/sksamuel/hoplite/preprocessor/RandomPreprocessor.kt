package com.sksamuel.hoplite.preprocessor

import kotlin.random.Random

object RandomPreprocessor : Preprocessor {
  private val regex = "\\\$RANDOM_STRING\\((\\d+)\\)".toRegex()
  private const val a = 33 // '!'
  private val z = 126 // '~'
  override fun process(value: String): String = regex.replace(value) {
    val length = it.groupValues[1].toInt()
    val chars = CharArray(length) { Random.nextInt(33, 126).toChar() }
    String(chars)
  }
}
