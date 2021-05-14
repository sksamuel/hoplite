package com.sksamuel.hoplite

import kotlin.reflect.KParameter

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  SnakeCaseParamMapper,
  KebabCaseParamMapper,
  AliasAnnotationParamMapper,
)

/**
 * A [ParameterMapper] takes a parameter and returns the name to be used to look
 * a config value for that parameter. This allows implementations to tweak the
 * name used for lookups.
 *
 * For example, the [SnakeCaseParamMapper] returns a name in in snake_case.
 * This allows you to define snake-case config-keys and map them to camel case
 * field names.
 *
 * Mappers stack, so that if multiple mappers return different names, then they
 * are all attempted in turn. This allows for instance, snake_case and kebab-case
 * to be mixed in the same project.
 */
interface ParameterMapper {
  fun map(param: KParameter): String
}

object DefaultParamMapper : ParameterMapper {
  override fun map(param: KParameter): String = param.name ?: "<anon>"
}

/**
 * Disabled by default so that common ENVVAR PARAMS don't override your lower case
 * names unexpectedly.
 */
object UppercaseParamMapper : ParameterMapper {
  override fun map(param: KParameter): String = param.name?.toUpperCase() ?: "<anon>"
}

annotation class ConfigAlias(val name: String)

object AliasAnnotationParamMapper : ParameterMapper {
  override fun map(param: KParameter): String {
    return param.annotations.filterIsInstance<ConfigAlias>().firstOrNull()?.name ?: param.name ?: "<anon>"
  }
}

/**
 * A [ParameterMapper] that will transform a parameter name into
 * the snake case equivalent.
 *
 * For example, camelCasePilsen will become snake_case_pilsen.
 */
object SnakeCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter): String {
    return (param.name ?: "<anon>").fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.toLowerCase().toString()
        char.isUpperCase() -> acc + "_" + char.toLowerCase()
        else -> acc + char
      }
    }
  }
}

/**
 * A [ParameterMapper] that will transform a parameter name into
 * the kebab case equivalent.
 *
 * For example, camelCasePilsen will become kebab_case_pilsen.
 */
object KebabCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter): String {
    return (param.name ?: "<anon>").fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.toLowerCase().toString()
        char.isUpperCase() -> acc + "-" + char.toLowerCase()
        else -> acc + char
      }
    }
  }

}
