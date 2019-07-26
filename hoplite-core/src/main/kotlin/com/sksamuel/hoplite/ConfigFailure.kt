package com.sksamuel.hoplite

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface ConfigFailure {

  /**
   * A human-readable description of the failure.
   */
  fun description(): String

  /**
   * The optional location of the failure.
   */
  fun location(): ConfigLocation?

  companion object {
    operator fun invoke(description: String): ConfigFailure = GenericFailure(description)
    fun missingPath(path: String, keys: Collection<String>): ConfigFailure = MissingPathFailure(path, keys)
    fun unsupportedType(type: KType): ConfigFailure = UnsupportedTypeFailure(type)
    inline fun <reified T> conversionFailure(v: Any?): ConfigFailure = ConversionFailure(T::class, v)
    fun throwable(t: Throwable): ConfigFailure = ThrowableFailure(t, null)
  }
}

data class NullForNonNull(val value: NullNode, val param: String) : ConfigFailure {
  override fun description(): String = "null value supplied for non-null field $param"
  override fun location(): ConfigLocation? = null
}

data class MissingPathFailure(val description: String, val location: ConfigLocation?) : ConfigFailure {
  constructor(path: String, keys: Collection<String>) : this("Path $path was not available (available keys $keys)", null)

  override fun description(): String = description
  override fun location(): ConfigLocation? = location
}

data class UnsupportedTypeFailure(val type: KType) : ConfigFailure {
  override fun description(): String = "Type $type is unsupported"
  override fun location(): ConfigLocation? = null
}

/**
 * A failure occurred because an exception was thrown during the reading process.
 *
 * @param throwable the exception thrown
 * @param location the optional location of the failure
 */
data class ThrowableFailure(val throwable: Throwable, val location: ConfigLocation?) : ConfigFailure {
  override fun description() = "${throwable.message}.${throwable.stackTrace.toList()}"
  override fun location(): ConfigLocation? = location
}

data class GenericFailure(val description: String) : ConfigFailure {
  override fun description(): String = description
  override fun location(): ConfigLocation? = null
}

/**
 * A failure occurred due to the inability to parse the configuration.
 *
 * @param msg the error message from the parser
 * @param location the optional location of the failure
 */
data class CannotParse(val msg: String, val location: ConfigLocation?) : ConfigFailure {
  override fun description() = "Unable to parse the configuration: $msg."
  override fun location(): ConfigLocation? = location
}

/**
 * A [ConfigFailure] used when a target type could not be created from a given value.
 * For example, if a field in data class was an int, but at runtime the configuration
 * tried to pass "hello" then this would result in a conversion failure.
 */
data class ConversionFailure(val description: String, val location: ConfigLocation?) : ConfigFailure {
  constructor(klass: KClass<*>, value: Any?) :
      this("Cannot convert ${value?.javaClass?.name}:$value to ${klass.qualifiedName}", null)

  override fun description() = description
  override fun location(): ConfigLocation? = location
}