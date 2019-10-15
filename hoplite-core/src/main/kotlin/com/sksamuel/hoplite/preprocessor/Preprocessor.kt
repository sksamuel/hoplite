package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.Decoder

/**
 * A [Preprocessor] applies a function to a [Node] before the [Node]
 * is passed to a [Decoder].
 */
interface Preprocessor {
  fun process(node: Node): Node
}

fun defaultPreprocessors() = listOf(
  EnvVarPreprocessor,
  SystemPropertyPreprocessor,
  RandomPreprocessor
)

abstract class PrefixProcessor(private val prefix: String) : Preprocessor {

  abstract fun handle(node: StringNode): Node

  override fun process(node: Node): Node = when (node) {
    is StringNode -> if (node.value.startsWith(prefix)) handle(node) else node
    else -> node
  }
}

