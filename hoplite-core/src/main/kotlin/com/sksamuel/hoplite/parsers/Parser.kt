package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Props
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.io.InputStream
import java.util.ServiceLoader

interface Parser {
  fun load(input: InputStream, source: String): Props
  fun defaultFileExtensions(): List<String>
}

interface ParserRegistry {

  fun locate(ext: String): ConfigResult<Parser>

  fun register(ext: String, parser: Parser): ParserRegistry

  /**
   * Returns the currently supported file mappings
   */
  fun registeredExtensions(): Set<String>

  companion object {
    val zero: ParserRegistry = DefaultParserRegistry(emptyMap())
  }
}

class DefaultParserRegistry(private val map: Map<String, Parser>) : ParserRegistry {

  override fun locate(ext: String): ConfigResult<Parser> {
    return map[ext]?.valid() ?: ConfigFailure.NoSuchParser(ext, map).invalid()
  }

  override fun registeredExtensions(): Set<String> = map.keys

  override fun register(ext: String, parser: Parser): ParserRegistry = DefaultParserRegistry(
    map.plus(ext to parser))
}

fun defaultParserRegistry(): ParserRegistry {
  return defaultParserRegistry(Thread.currentThread().contextClassLoader)
}

fun defaultParserRegistry(classLoader: ClassLoader): ParserRegistry {
  return ServiceLoader.load(Parser::class.java, classLoader).toList()
    .fold(ParserRegistry.zero) { registry, parser ->
      parser.defaultFileExtensions().fold(registry) { r, ext -> r.register(ext, parser) }
    }
}
