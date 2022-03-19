package com.sksamuel.hoplite

class Reporter {

  fun report(sources: List<PropertySource>): String {
    return "\n${sources.size} sources (highest to lowest priority):\n" +
      sources.joinToString("\n  - ", "  - ", "\n") { it.source() }
  }

  fun report(node: Node): String {
    val keys = report("", node)
    val keyPad = keys.maxOf { it.key.length }
    val sourcePad = keys.maxOf { it.source.length }
    val valuePad = keys.maxOf { it.value.length }

    val bar = listOf("".padEnd(keyPad + 2, '-'), "".padEnd(sourcePad + 2, '-'), "".padEnd(valuePad + 2, '-'))
      .joinToString("+", "+", "+")

    val titles = listOf("Key".padEnd(keyPad, ' '), "Source".padEnd(sourcePad, ' '), "Value".padEnd(valuePad, ' '))
      .joinToString(" | ", "| ", " |")

    val components = listOf(bar, titles, bar) + keys.map {
      "| " + it.key.padEnd(keyPad, ' ') +
        " | " + it.source.padEnd(sourcePad, ' ') +
        " | " + it.value.padEnd(valuePad, ' ') + " |"
    } + listOf(bar)
    return components.joinToString("\n", "\n", "\n")
  }

  private fun report(name: String, node: Node): List<KeyUsage> {
    return when (node) {
      is ArrayNode -> emptyList()
      is MapNode -> node.map.entries.flatMap { (key, value) -> report("$name.$key".removePrefix("."), value) }
      is BooleanNode -> listOf(KeyUsage(name, node.pos.source() ?: "n/a", node.value.toString()))
      is NullNode -> listOf(KeyUsage(name, node.pos.source() ?: "n/a", "<null>"))
      is DoubleNode -> listOf(KeyUsage(name, node.pos.source() ?: "n/a", node.value.toString()))
      is LongNode -> listOf(KeyUsage(name, node.pos.source() ?: "n/a", node.value.toString()))
      is StringNode -> listOf(KeyUsage(name, node.pos.source() ?: "n/a", node.value))
      Undefined -> emptyList()
    }
  }
}

data class KeyUsage(
  val key: String,
  val source: String,
  val value: String,
)
