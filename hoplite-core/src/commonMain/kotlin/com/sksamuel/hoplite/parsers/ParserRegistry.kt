package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ConfigResult

interface ParserRegistry {

  fun locate(ext: String): ConfigResult<Parser>

  fun register(ext: String, parser: Parser): ParserRegistry

  /**
   * Returns the currently supported file mappings
   */
  fun registeredExtensions(): Set<String>

  companion object {
    val zero: ParserRegistry = TODO()// DefaultParserRegistry(emptyMap())
  }
}
