package com.sksamuel.hoplite

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

/**
 * A [ParameterMapper] takes a data class parameter and returns one or more names
 * to be used to look up a value for that parameter. This allows implementations
 * to support property sources defining names differently from code.
 *
 * For example, the [SnakeCaseParamMapper] returns a name in snake_case.
 * This allows you to use snake-case config-keys and map them to camel case
 * field names.
 *
 * Mappers stack, so that if multiple mappers return different names, then they
 * are all attempted in turn. This allows for instance, snake_case and kebab-case
 * to be mixed in the same project.
 */
interface ParameterMapper {

  /**
   * Returns zero or more names to use when looking up a value for this parameter.
   *
   * @param param the parameter in the constructor
   * @param constructor the constructor that defined the param.
   * @param kclass the kclass that defined the constructor
   */
  fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String>
}

object DefaultParamMapper : ParameterMapper {
  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> =
    setOfNotNull(param.name)
}

object LowercaseParamMapper : ParameterMapper {
  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> =
    setOfNotNull(param.name?.lowercase())
}

/**
 * Disabled by default so that common ENVVAR PARAMS don't override your lower case
 * names unexpectedly.
 */
object UppercaseParamMapper : ParameterMapper {
  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> =
    setOfNotNull(param.name?.uppercase())
}

@Repeatable
annotation class ConfigAlias(val name: String)

object AliasAnnotationParamMapper : ParameterMapper {
  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    return param.annotations.filterIsInstance<ConfigAlias>().map { it.name }.toSet()
  }
}

/**
 * A [ParameterMapper] that will transform a parameter name into
 * the snake case equivalent.
 *
 * For example, camelCasePilsen will become snake_case_pilsen.
 *
 * When using the [PathNormalizer] (which is enabled by default), this mapper is unnecessary.
 */
object SnakeCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val name = param.name ?: return emptySet()
    val snake = name.fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.lowercaseChar().toString()
        char.isUpperCase() -> acc + "_" + char.lowercaseChar()
        else -> acc + char
      }
    }
    val trailingDigits = snake.trailingDigits()
    val lastNumberVariant =
      if (trailingDigits.isEmpty()) null else snake.removeSuffix(trailingDigits) + "_" + trailingDigits
    return setOfNotNull(snake, lastNumberVariant)
  }
}

/**
 * A [ParameterMapper] that will transform a parameter name into
 * the kebab case equivalent.
 *
 * For example, camelCasePilsen will become kebab-case-pilsen.
 *
 * When using the [PathNormalizer] (which is enabled by default), this mapper is unnecessary.
 */
object KebabCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val name = param.name ?: return emptySet()
    val kebab = name.fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.lowercaseChar().toString()
        char.isUpperCase() -> acc + "-" + char.lowercaseChar()
        else -> acc + char
      }
    }
    val trailingDigits = kebab.trailingDigits()
    val lastNumberVariant =
      if (trailingDigits.isEmpty()) null else kebab.removeSuffix(trailingDigits) + "-" + trailingDigits
    return setOfNotNull(kebab, lastNumberVariant)
  }
}

private fun String.trailingDigits(): String {
  if (this.isEmpty()) return ""
  if (!this.last().isDigit()) return ""
  return dropLast(1).trailingDigits() + this.last()
}
