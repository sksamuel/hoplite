package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import java.util.*

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
