package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

/**
 * An implementation of [PropertySource] that provides config based on command line arguments.
 *
 * Parameters will be processed if they start with a given prefix. Key and value are split by a given delimiter.
 */
class CommandLinePropertySource(
  private val arguments: Array<String>,
  private val prefix: String,
  private val delimiter: String,
) : PropertySource {

  override fun source(): String = "Arguments delimited by $delimiter"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val values = arguments.asSequence().filter {
      it.startsWith(prefix) && it.contains(delimiter)
    }.map {
      it.removePrefix(prefix).split(delimiter, limit = 2)
    }.groupingBy {
      it[0]
    }.aggregate { _, accumulator: Any?, element, _ ->
      when (accumulator) {
        null -> element[1]
        is List<*> -> accumulator + element[1]
        else -> listOf(accumulator, element[1])
      }
    }
    return when {
      values.isEmpty() -> Undefined.valid()
      else -> values.toNode("commandLine").valid()
    }
  }
}
