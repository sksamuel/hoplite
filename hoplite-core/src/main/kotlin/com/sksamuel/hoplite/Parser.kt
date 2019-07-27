package com.sksamuel.hoplite

import java.io.InputStream
import java.util.*

interface Parser {
  fun load(input: InputStream): Node
  fun defaultFileExtensions(): List<String>
}

interface ParserRegistry {
  fun locate(ext: String): Parser?
  fun register(ext: String, parser: Parser): ParserRegistry

  companion object {
    val zero: ParserRegistry = DefaultParserRegistry(emptyMap())
  }
}

class DefaultParserRegistry(private val map: Map<String, Parser>) : ParserRegistry {
  override fun locate(ext: String): Parser? = map[ext]
  override fun register(ext: String, parser: Parser): ParserRegistry = DefaultParserRegistry(map.plus(ext to parser))
}

fun defaultParserRegistry(): ParserRegistry {
  return ServiceLoader.load(Parser::class.java).toList()
      .fold(ParserRegistry.zero) { registry, parser ->
        parser.defaultFileExtensions().fold(registry) { r, ext -> r.register(ext, parser) }
      }
}