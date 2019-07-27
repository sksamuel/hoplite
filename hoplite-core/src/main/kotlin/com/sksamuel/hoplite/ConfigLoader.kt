package com.sksamuel.hoplite

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.preprocessor.EnvVarPreprocessor
import com.sksamuel.hoplite.preprocessor.Preprocessor
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigLoader(private val decoderRegistry: DecoderRegistry = defaultDecoderRegistry(),
                   private val parserRegistry: ParserRegistry = defaultParserRegistry(),
                   private val preprocessors: List<Preprocessor> = listOf(EnvVarPreprocessor)) {

  fun withPreprocessor(preprocessor: Preprocessor) = ConfigLoader(
      decoderRegistry,
      parserRegistry,
      preprocessors + preprocessor)

  fun withDecoder(decoder: Decoder<*>) = ConfigLoader(
      decoderRegistry.register(decoder),
      parserRegistry,
      preprocessors)

  fun withFileExtensionMapping(ext: String, parser: Parser) = ConfigLoader(
      decoderRegistry,
      parserRegistry.register(ext, parser),
      preprocessors)

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A =
      loadConfig<A>(*resources).fold(
          { errors -> throw RuntimeException("Error loading config\n" + errors.all.joinToString("\n") { it.description() }) },
          { it }
      )

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> = loadConfig(A::class, *resources)

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  fun <A : Any> loadConfig(klass: KClass<A>, vararg resources: String): ConfigResult<A> {

    data class Input(val resource: String, val stream: InputStream, val parser: Parser, val ext: String)

    val streams = resources.map { resource ->
      this.javaClass.getResourceAsStream(resource).toOption().fold(
          { ConfigFailure("Could not find resource $resource").invalidNel() },
          { stream ->
            val ext = resource.split('.').last()
            val parser = parserRegistry.locate(ext)?.validNel() ?: ConfigResults.NoSuchParser(ext)
            parser.map { Input(resource, stream, it, ext) }
          }
      )
    }.sequence()

    val root = streams.map {
      it.map { input -> input.parser.load(input.stream) }
    }.map { cs ->
      cs.map { c ->
        preprocessors.fold(c) { acc, p -> acc.transform(p::process) }
      }.reduce { acc, b -> acc.withFallback(b) }
    }

    return root.flatMap { node ->
      decoderRegistry.decoder(klass).flatMap { decoder ->
        decoder.decode(node, klass.createType(), decoderRegistry)
      }
    }
  }
//    return cursors.map {
//      it.reduce { a, b -> a.withFallback(b) }
//    }.flatMap {
//      DataClassConverter(klass).apply(it)
//    }
}

//  fun toCursor(stream: InputStream): ConfigResult<Cursor> = handleYamlErrors(stream) {
//    val yaml = Yaml(SafeConstructor())
//    when (val result = yaml.load<Any>(it)) {
//      is Map<*, *> -> MapCursor(result).validNel()
//      else -> ConfigFailure("Unsupported YAML return type ${result.javaClass.name}").invalidNel()
//    }
//  }
//
//  fun <A> handleYamlErrors(stream: InputStream, f: (InputStream) -> ConfigResult<A>): ConfigResult<A> =
//      try {
//        f(stream)
//      } catch (e: MarkedYAMLException) {
//        CannotParse(e.message!!, locationFromMark(Paths.get("/todo"), e.problemMark)).invalidNel()
//      } catch (t: Throwable) {
//        ConfigFailure.throwable(t).invalidNel()
//      }
//
//  fun locationFromMark(path: Path, mark: Mark): ConfigLocation = ConfigLocation(path.toUri().toURL(), mark.line)