package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${some.path} by looking up some.path in the parsed config.
 * Defaults can also be applied in case the path does not exist: ${var:-some.path}
 */
object LookupPreprocessor : Preprocessor {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(.*?)\\}".toRegex()
  private val valueWithDefaultRegex = "(.*?):-(.*?)".toRegex()


  override fun process(node: Node): Node {

    fun lookup(key: String): String? {
      return when (val n = node.atPath(key)) {
        is StringNode -> n.value
        else -> null
      }
    }

    fun handle(n: Node): Node = when (n) {
      is MapNode -> MapNode(n.map.map { (k, v) -> k to handle(v) }.toMap(), n.pos)
      is ArrayNode -> ArrayNode(n.elements.map { handle(it) }, n.pos)
      is StringNode -> {
        val value = regex.replace(n.value) { result ->
          val key = result.groupValues[1]
          when (val matchWithDefault = valueWithDefaultRegex.matchEntire(key)) {
            // no default so we use the env key or return whatever the original string was
            null -> lookup(key) ?: result.value
            // lookup with default value fallback
            else -> matchWithDefault.let { m -> lookup(m.groups[1]!!.value) ?: m.groups[2]!!.value }
          }
        }
        n.copy(value = value)
      }
      else -> n
    }

    return handle(node)
  }
}
