package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

object EnvVarPreprocessor : Preprocessor {

  private val regex = "\\$\\{(.*?)}".toRegex()

  override fun process(node: Node): Node = when (node) {
    is StringNode -> {
      val value = regex.replace(node.value) {
        val key = it.groupValues[1]
        System.getenv(key) ?: it.value
      }
      node.copy(value = value)
    }
    else -> node
  }
}
