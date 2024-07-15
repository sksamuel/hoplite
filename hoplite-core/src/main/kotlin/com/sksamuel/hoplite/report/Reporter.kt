package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.NodeState
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.internal.DecodingState
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.valueOrNull
import kotlin.math.max

@Deprecated("Specify reporter options through ConfigBuilderLoader")
class ReporterBuilder {

  private var print: Print = { println(it) }
  private var obfuscator: Obfuscator = PrefixObfuscator(3)

  fun withPrint(print: Print) = apply {
    this.print = print
  }

  @Deprecated("Specify obfuscator through ConfigBuilderLoader")
  fun withObfuscator(obfuscator: Obfuscator) = apply {
    this.obfuscator = obfuscator
  }

  @Deprecated("Specify secretsPolicy through ConfigBuilderLoader", level = DeprecationLevel.ERROR)
  fun withSecretsPolicy(secretsPolicy: SecretsPolicy): ReporterBuilder = TODO("Unsupported")

  fun build(): Reporter = Reporter(print, obfuscator, null)
}

typealias Print = (String) -> Unit

class Reporter(
  private val print: Print,
  private val obfuscator: Obfuscator,
  private val environment: Environment?
) {

  object Titles {
    const val Key = "Key"
    const val Source = "Source"
    const val SourceKey = "Source Key"
    const val Value = "Value"
  }

  companion object {
    fun default(): Reporter = ReporterBuilder().build()
  }

  fun printReport(
    sources: List<PropertySource>,
    state: DecodingState,
    sections: Map<String, List<Map<String, Any?>>>
  ) {

    val r = buildString {
      appendLine()
      appendLine("--Start Hoplite Config Report---")
      appendLine()
      environment?.let { appendLine("Environment: ${it.name}") }
      appendLine()
      appendLine(report(sources))
      appendLine()

      val used = state.states.filter { it.used }
      if (used.isEmpty()) appendLine("Used keys: none")
      if (used.isNotEmpty()) appendLine(reportNodes(used, "Used keys"))

      appendLine()

      val unused = state.states.filterNot { it.used }
      if (unused.isEmpty()) appendLine("Unused keys: none")
      if (unused.isNotEmpty()) appendLine(reportNodes(unused, "Unused keys"))

      appendLine()
      appendLine(reportSections(sections))

      appendLine("--End Hoplite Config Report--")
      appendLine()
    }

    print(r)
  }

  fun report(sources: List<PropertySource>): String {
    return "Property sources (highest to lowest priority):" + System.lineSeparator() +
      sources.joinToString(System.lineSeparator() + "  - ", "  - ") { it.source() }
  }

  private fun reportNodes(nodes: List<NodeState>, title: String?): String {
    require(nodes.isNotEmpty())

    // copy the notes, obfuscating if a secret, and turning all nodes into string nodes for reporting ease
    val obfuscated: List<NodeState> = nodes.sortedBy { it.node.path.flatten() }.map { state ->

      val value = if (state.secret && state.node is PrimitiveNode)
        obfuscator.obfuscate(state.node).replace("\n", "")
      else
        state.node.valueOrNull()?.replace("\n", "")

      state.copy(
        node = StringNode(
          value = value ?: "<null>",
          pos = state.node.pos,
          path = state.node.path,
          meta = state.node.meta,
          sourceKey = state.node.sourceKey,
        )
      )
    }

    val keyPadded = max(Titles.Key.length, nodes.maxOf { it.node.path.flatten().length })
    val sourcePadded = nodes.maxOf { max(it.node.pos.source()?.length ?: 0, Titles.Source.length) }
    val sourceKeyPadded = max(Titles.SourceKey.length, nodes.maxOf { it.node.sourceKey.orEmpty().length })
    val valuePadded = max(Titles.Value.length, obfuscated.maxOf { (it.node as StringNode).value.length })

    val rows = obfuscated.map {
      listOfNotNull(
        it.node.path.flatten().padEnd(keyPadded, ' '),
        (it.node.pos.source() ?: "").padEnd(sourcePadded, ' '),
        it.node.sourceKey.orEmpty().padEnd(sourceKeyPadded, ' '),
        (it.node as StringNode).value.padEnd(valuePadded, ' ')
      ).joinToString(" | ", "| ", " |")
    }

    val titleRow = title?.let { "$it: ${nodes.size}" }

    val bar = listOfNotNull(
      "".padEnd(keyPadded + 2, '-'),
      "".padEnd(sourcePadded + 2, '-'),
      "".padEnd(sourceKeyPadded + 2, '-'),
      "".padEnd(valuePadded + 2, '-')
    ).joinToString("+", "+", "+")

    val titles = listOfNotNull(
      Titles.Key.padEnd(keyPadded, ' '),
      Titles.Source.padEnd(sourcePadded, ' '),
      Titles.SourceKey.padEnd(sourceKeyPadded, ' '),
      Titles.Value.padEnd(valuePadded, ' ')
    ).joinToString(" | ", "| ", " |")

    return (listOfNotNull(titleRow, bar, titles, bar) + rows + listOf(bar)).joinToString(System.lineSeparator())
  }

  private fun reportSections(sections: Map<String, List<Map<String, Any?>>>): String {
    return buildString {
      sections.forEach { (section, rows) ->

        val keys = rows.flatMap { map -> map.map { it.key } }.distinct()
        val values = rows.map { row -> keys.associateWith { row[it]?.toString() ?: "" } }
        val pads = keys.associateWith { key -> max(key.length, values.mapNotNull { it[key] }.maxOf { it.length }) }
        val bar = keys.joinToString("+", "+", "+") { "".padEnd((pads[it] ?: 0) + 2, '-') }
        val titles = keys.joinToString(" | ", "| ", " |") { it.padEnd(pads[it] ?: 0, ' ') }

        appendLine(section)
        appendLine(bar)
        appendLine(titles)
        appendLine(bar)
        values.forEach { row ->
          appendLine(row.entries.joinToString(" | ", "| ", " |") { it.value.padEnd((pads[it.key] ?: 0)) })
        }
        appendLine(bar)
        appendLine()
      }
    }
  }
}
