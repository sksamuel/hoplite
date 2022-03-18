@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.UnresolvedSubstitutionChecker
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader constructor(
  private val decoderRegistry: DecoderRegistry,
  private val propertySources: List<PropertySource>,
  private val parserRegistry: ParserRegistry,
  private val preprocessors: List<Preprocessor>,
  private val paramMappers: List<ParameterMapper>,
  private val onFailure: List<(Throwable) -> Unit> = emptyList(),
  private val mode: DecodeMode = DecodeMode.Lenient,
) {

  companion object {

    /**
     * Returns a [ConfigLoader] with default options applied.
     */
    operator fun invoke(): ConfigLoader = ConfigLoaderBuilder.default().build()

    /**
     * Returns a [ConfigLoaderBuilder] with default options applied, which can be further customized.
     */
    fun builder(): ConfigLoaderBuilder = ConfigLoaderBuilder.default()

    /**
     * Returns a [ConfigLoader] with default options with the given [block] applied.
     *
     * Eg,
     *
     * val loader = ConfigLoader {
     *   addDecoder(MyDecoder)
     *   addPropertySource(propSource)
     * }
     *
     */
    inline operator fun invoke(block: ConfigLoaderBuilder.() -> Unit): ConfigLoader {
      return ConfigLoaderBuilder.default().apply(block).build()
    }
  }

  /**
   * Attempts to load config and returns an instance of [A] if the values can be appropriately converted,
   * otherwise throws an exception.
   */
  inline fun <reified A : Any> loadConfigOrThrow(): A = loadConfig<A>().returnOrThrow()

  /**
   * Attempts to load config and returns a [ConfigResult] with either the errors during load,
   * or the successfully created instance A.
   */
  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class)

  /**
   * Attempts to load config and returns a [ConfigResult] with either the errors during load,
   * or the successfully created instance A.
   */
  fun <A : Any> loadConfigOrThrow(klass: KClass<A>): A =
    loadConfig(klass).returnOrThrow()

  /**
   * Attempts to load config into a [Node] and returns a [ConfigResult] with either the errors during load,
   * or the fully resolved node.
   */
  fun loadNodeOrThrow(): Node = loadNode().returnOrThrow()

  fun <A : Any> loadConfig(klass: KClass<A>): ConfigResult<A> {
    // This is where the actual processing takes place. All other loadConfig or throw methods
    // ultimately end up in this method.
    require(klass.isData) { "Can only decode into data classes [was ${klass}]" }
    return if (decoderRegistry.size == 0)
      ConfigFailure.EmptyDecoderRegistry.invalid()
    else
      loadNode().flatMap { decode(klass, it) }
  }

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.getOrElse { failure ->
    val err = "Error loading config because:\n\n" + failure.description().indent(Constants.indent)
    onFailure.forEach { it(ConfigException(err)) }
    throw ConfigException(err)
  }

  private fun <A : Any> decode(kclass: KClass<A>, node: Node): ConfigResult<A> {
    return decoderRegistry.decoder(kclass).flatMap { decoder ->
      val context = DecoderContext(decoderRegistry, paramMappers, preprocessors, mode)
      val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
      val errors = UnresolvedSubstitutionChecker.process(preprocessed)
      if (errors.isNotEmpty())
        ConfigFailure.MultipleFailures(NonEmptyList(errors)).invalid()
      else
        decoder.decode(preprocessed, kclass.createType(), context)
    }
  }

  /**
   * Loads all property sources and combines them into a single node.
   */
  private fun loadNode(): ConfigResult<Node> {
    return propertySources.map { it.node(PropertySourceContext(parserRegistry)) }.sequence()
      .map { it.takeUnless { it.isEmpty() }?.reduce { acc, b -> acc.merge(b) } ?: NullNode(Pos.NoPos) }
      .mapInvalid {
        val multipleFailures = ConfigFailure.MultipleFailures(it)
        multipleFailures
      }
  }
}

