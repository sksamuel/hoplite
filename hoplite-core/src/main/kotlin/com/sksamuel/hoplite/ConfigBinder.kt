package com.sksamuel.hoplite

import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.internal.ConfigParser
import kotlin.reflect.KClass

/**
 * Maintains a parsed configuration which can be used to bind to multiple configuration classes with a prefix.
 */
class ConfigBinder(
  @PublishedApi internal val configParser: ConfigParser,
  @PublishedApi internal val environment: Environment?,
  private val onFailure: List<(Throwable) -> Unit> = emptyList(),
) {
  /**
   * Binds the configuration to a configuration class, looking at  with the given prefix.
   */
  inline fun <reified A : Any> bind(prefix: String): ConfigResult<A> = bind(A::class, prefix)

  /**
   * Binds the configuration to a configuration class, looking at  with the given prefix.
   */
  inline fun <reified A : Any> bindOrThrow(prefix: String): A = bindOrThrow(A::class, prefix)

  /**
   * Binds the configuration to a configuration class with the given prefix.
   */
  fun <A : Any> bind(type: KClass<A>, prefix: String): ConfigResult<A> =
    configParser.decode(type, environment, prefix)

  /**
   * Binds the configuration to a configuration class with the given prefix, or throws if the result is not valid.
   */
  fun <A : Any> bindOrThrow(type: KClass<A>, prefix: String): A =
    configParser.decode(type, environment, prefix).returnOrThrow(onFailure)
}
