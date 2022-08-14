package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.DecodingState
import com.sksamuel.hoplite.NodeState
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretStrength
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.valueOrNull
import kotlin.math.max

@Deprecated("Specify options through ConfigBuilderLoader")
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

  fun build(): Reporter = Reporter(print, obfuscator)
}

typealias Print = (String) -> Unit

class Reporter(
  private val print: Print,
  private val obfuscator: Obfuscator,
) {

  object Titles {
    const val Remote = "Remote Lookup"
    const val Strength = "Strength"
  }

  companion object {
    fun default(): Reporter = ReporterBuilder().build()
  }

  fun printReport(
    sources: List<PropertySource>,
    state: DecodingState,
  ) {

    val r = buildString {
      appendLine()
      appendLine("--Start Hoplite Config Report---")
      appendLine()
      appendLine(report(sources))
      appendLine()
      if (state.used.isEmpty()) appendLine("Used keys: none")
      if (state.used.isNotEmpty()) appendLine(reportNodes(state.states.filter { it.used }, "Used keys"))

      appendLine()

      if (state.unused.isEmpty()) appendLine("Unused keys: none")
      if (state.unused.isNotEmpty()) appendLine(reportNodes(state.states.filterNot { it.used }, "Unused keys"))

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

  private fun reportNodes(nodes: List<NodeState>, title: String?): String {

    // copy the notes, obfuscating if a secret, and turning all nodes into string nodes for reporting ease
    val obfuscated: List<NodeState> = nodes.sortedBy { it.node.path.flatten() }.map { state ->

      val value = if (state.secret && state.node is PrimitiveNode)
        obfuscator.obfuscate(state.node)
      else
        state.node.valueOrNull()

      state.copy(
        node = StringNode(
          value = value ?: "<null>",
          pos = state.node.pos,
          path = state.node.path,
          meta = state.node.meta
        )
      )
    }

    val keyPadded = nodes.maxOf { it.node.path.flatten().length }
    val sourcePadded = nodes.maxOf { max(it.node.pos.source()?.length ?: 0, "source".length) }
    val valuePadded = max("Value".length, obfuscated.maxOf { (it.node as StringNode).value.length })
    val strengthPadded = max(Titles.Strength.length, nodes.maxOf { it.secretStrength?.asString()?.length ?: 0 })
    val remotePadded = max(Titles.Remote.length, nodes.maxOf {
      it.node.meta[CommonMetadata.RemoteLookup]?.toString()?.length ?: 0
    })

    val rows = obfuscated.map {
      listOf(
        it.node.path.flatten().padEnd(keyPadded, ' '),
        (it.node.pos.source() ?: "").padEnd(sourcePadded, ' '),
        (it.node as StringNode).value.padEnd(valuePadded, ' '),
        it.secretStrength.asString().padEnd(strengthPadded, ' '),
        (it.node.meta[CommonMetadata.RemoteLookup]?.toString() ?: "").padEnd(remotePadded, ' '),
      ).joinToString(" | ", "| ", " |")
    }

    val titleRow = title?.let { "$it: ${nodes.size}" }

    val bar = listOf(
      "".padEnd(keyPadded + 2, '-'),
      "".padEnd(sourcePadded + 2, '-'),
      "".padEnd(valuePadded + 2, '-'),
      "".padEnd(strengthPadded + 2, '-'),
      "".padEnd(remotePadded + 2, '-'),
    ).joinToString("+", "+", "+")

    val titles = listOf(
      "Key".padEnd(keyPadded, ' '),
      "Source".padEnd(sourcePadded, ' '),
      "Value".padEnd(valuePadded, ' '),
      Titles.Strength.padEnd(strengthPadded, ' '),
      Titles.Remote.padEnd(remotePadded, ' '),
    ).joinToString(" | ", "| ", " |")

    return (listOfNotNull(titleRow, bar, titles, bar) + rows + listOf(bar)).joinToString(System.lineSeparator())
  }
}

fun SecretStrength?.asString() = when (this) {
  null -> ""
  SecretStrength.Strong -> "Strong"
  is SecretStrength.Weak -> "WEAK - " + this.reason
}
