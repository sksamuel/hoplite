package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

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
