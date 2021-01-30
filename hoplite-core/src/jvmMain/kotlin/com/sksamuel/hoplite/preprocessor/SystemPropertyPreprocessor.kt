package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode

object SystemPropertyPreprocessor : TraversingPrimitivePreprocessor() {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(.*?)\\}".toRegex()

  override fun handle(node: PrimitiveNode): Node = when (node) {
    is StringNode -> {
      val value = regex.replace(node.value) {
        val key = it.groupValues[1]
        System.getProperty(key, it.value)
      }
      node.copy(value = value)
    }
    else -> node
  }

}
