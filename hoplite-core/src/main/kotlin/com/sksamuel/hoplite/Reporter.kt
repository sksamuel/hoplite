package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath

class Reporter(private val print: (String) -> Unit) {
  constructor() : this({ println(it) })

  fun printReport(
    sources: List<PropertySource>,
    node: Node,
    used: List<Pair<DotPath, Pos>>,
  ) {

    val r = buildString {
      appendLine()
      appendLine("--Start Hoplite Config Report---")
      appendLine()
      appendLine(report(sources))
      appendLine()

      val usedPaths = used.map { it.first }
      val (usedResources, unusedResources) = node.resources().partition { usedPaths.contains(it.path) }

      if (used.isEmpty()) appendLine("Used keys: none")
      if (used.isNotEmpty()) appendLine(reportPaths(usedResources, "Used"))

      appendLine()

      if (unusedResources.isEmpty()) appendLine("Unused keys: none")
      if (unusedResources.isNotEmpty()) appendLine(reportPaths(unusedResources, "Unused"))

      appendLine()
      appendLine("--End Hoplite Config Report--")
      appendLine()
    }

    print(r)
  }

  fun report(sources: List<PropertySource>): String {
    return "Property sources (highest to lowest priority):" + System.lineSeparator() +
      sources.joinToString(System.lineSeparator() + "  - ", "  - ") { it.source() }
  }

  fun reportPaths(resources: List<ConfigResource>, title: String): String {

    val keyPadded = resources.maxOf { it.path.flatten().length }
    val sourcePadded = resources.maxOf { it.source.length }
    val valuePadded = resources.maxOf { it.value.length }

    val usedCount = "$title keys ${resources.size}"

    val bar = listOf(
      "".padEnd(keyPadded + 2, '-'),
      "".padEnd(sourcePadded + 2, '-'),
      "".padEnd(valuePadded + 2, '-')
    ).joinToString("+", "+", "+")

    val titles =
      listOf(
        "Key".padEnd(keyPadded, ' '),
        "Source".padEnd(sourcePadded, ' '),
        "Value".padEnd(valuePadded, ' ')
      ).joinToString(" | ", "| ", " |")

    val components = listOf(usedCount, bar, titles, bar) + resources.map {
      "| " + it.path.flatten().padEnd(keyPadded, ' ') +
        " | " + it.source.padEnd(sourcePadded, ' ') +
        " | " + it.value.padEnd(valuePadded, ' ') + " |"
    } + listOf(bar)
    return components.joinToString(System.lineSeparator())
  }
}

fun Node.resources(): List<ConfigResource> {
  return when (this) {
    is ArrayNode -> emptyList()
    is MapNode -> map.entries.map { (_, value) -> value.resources() }.flatten()
    is BooleanNode -> listOf(ConfigResource(path, pos.source() ?: "n/a", value.toString()))
    is NullNode -> listOf(ConfigResource(path, pos.source() ?: "n/a", "<null>"))
    is DoubleNode -> listOf(ConfigResource(path, pos.source() ?: "n/a", value.toString()))
    is LongNode -> listOf(ConfigResource(path, pos.source() ?: "n/a", value.toString()))
    is StringNode -> listOf(ConfigResource(path, pos.source() ?: "n/a", value))
    Undefined -> emptyList()
  }
}

data class ConfigResource(
  val path: DotPath,
  val source: String,
  val value: String,
)
