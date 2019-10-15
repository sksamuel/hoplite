package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

abstract class StringNodePreprocessor : Preprocessor {

  override fun process(node: Node): Node = when (node) {
    is StringNode -> map(node)
    else -> node
  }

  protected abstract fun map(node: StringNode): Node
}

object SystemPropertyPreprocessor : StringNodePreprocessor() {

  private val regex = "\\$\\{(.*?)}".toRegex()

  override fun map(node: StringNode): Node {
    val value = regex.replace(node.value) {
      val key = it.groupValues[1]
      System.getProperty(key, it.value)
    }
    return node.copy(value = value)
  }

}
