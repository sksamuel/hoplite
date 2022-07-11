@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid

/**
 * A [Preprocessor] applies a function to a root [Node] before that root node is
 * used to decode values.
 *
 * It is the responsibility of the preprecessors to traverse the node tree applying
 * to child nodes if applicable.
 *
 * A [TraversingPrimitivePreprocessor] is available to subclass
 * which will perform the task of descending into child nodes when a container node
 * is encountered.
 */
interface Preprocessor {
  fun process(node: Node): ConfigResult<Node>
}

object UnresolvedSubstitutionChecker {

  private val regex = "\\$\\{(.*?)\\}".toRegex()

  fun process(node: Node): ConfigResult<Node> {
    val errors = traverse(node)
    return if (errors.isEmpty()) node.valid() else ConfigFailure.MultipleFailures(NonEmptyList.unsafe(errors)).invalid()
  }

  private fun traverse(node: Node): List<ConfigFailure> = when (node) {
    is MapNode -> {
      val a = when (node.value) {
        is StringNode -> check(node.value)
        else -> null
      }
      listOfNotNull(a) + node.map.flatMap { (_, v) -> traverse(v) }
    }
    is ArrayNode -> node.elements.flatMap { traverse(it) }
    is StringNode -> listOfNotNull(check(node))
    else -> emptyList()
  }

  private fun check(node: StringNode): ConfigFailure.UnresolvedSubstitution? {
    return if (regex.containsMatchIn(node.value))
      ConfigFailure.UnresolvedSubstitution(node.value, node)
    else
      null
  }
}

/**
 * A preprocessor that will traverse the child nodes of maps and arrays, invoking [handle]
 * for any primitive nodes encountered.
 */
abstract class TraversingPrimitivePreprocessor : Preprocessor {

  abstract fun handle(node: PrimitiveNode): ConfigResult<Node>

  override fun process(node: Node): ConfigResult<Node> = when (node) {
    is MapNode -> {
      node.map.map { (k, v) -> process(v).map { k to it } }.sequence()
        .mapInvalid { ConfigFailure.MultipleFailures(it) }
        .map { it.toMap() }.flatMap { map ->
          val value = if (node.value is PrimitiveNode) handle(node.value) else node.value.valid()
          value.map { v ->
            MapNode(map, node.pos, node.path, v)
          }
        }
    }
    is ArrayNode -> {
      node.elements.map { process(it) }.sequence()
        .mapInvalid { ConfigFailure.MultipleFailures(it) }
        .map { ArrayNode(it, node.pos, node.path) }
    }
    is PrimitiveNode -> handle(node)
    else -> node.valid()
  }
}
