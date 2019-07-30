package com.sksamuel.hoplite

import arrow.core.toOption
import arrow.data.invalid
import arrow.data.valid
import java.io.InputStream
import java.util.*

interface Parser {
  fun load(input: InputStream, source: String): Node
  fun defaultFileExtensions(): List<String>
}

interface ParserRegistry {

  fun locate(ext: String): ConfigResult<Parser>

  fun register(ext: String, parser: Parser): ParserRegistry

  companion object {
    val zero: ParserRegistry = DefaultParserRegistry(emptyMap())
  }
}

class DefaultParserRegistry(private val map: Map<String, Parser>) : ParserRegistry {

  override fun locate(ext: String): ConfigResult<Parser> {
    return map[ext].toOption().fold({ ConfigFailure.NoSuchParser(ext).invalid() }, { it.valid() })
  }

  override fun register(ext: String, parser: Parser): ParserRegistry = DefaultParserRegistry(map.plus(ext to parser))
}

fun defaultParserRegistry(): ParserRegistry {
  return ServiceLoader.load(Parser::class.java).toList()
      .fold(ParserRegistry.zero) { registry, parser ->
        parser.defaultFileExtensions().fold(registry) { r, ext -> r.register(ext, parser) }
      }
}
