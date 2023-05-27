package com.sksamuel.hoplite

/**
 * Allows [Resolver]s and [Decoder]s to add content to the output report.
 */
class Reporter {

  private val sections: MutableMap<String, List<Map<String, Any?>>> = mutableMapOf()

  fun getReport(): Map<String, List<Map<String, Any?>>> = sections.toMap()

  /**
   * Adds a [row] to the named [section] in the report.
   */
  fun report(section: String, row: Map<String, Any?>) {
    val rows = sections.getOrPut(section) { emptyList() }
    sections[section] = (rows + row).distinct()
  }
}
