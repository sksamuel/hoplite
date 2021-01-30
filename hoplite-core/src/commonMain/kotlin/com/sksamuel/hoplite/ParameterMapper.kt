package com.sksamuel.hoplite

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  SnakeCaseParamMapper,
  KebabCaseParamMapper
)

/**
 * A [ParameterMapper] takes a parameter name and returns a converted name that should
 * be used to look a config value. This allows implementations to tweak the name used for lookups.
 *
 * For example, the [SnakeCaseParamMapper] returns a name in in snake_case.
 * This allows you to define snake-case config-keys* and map them to camel case
 * field names.
 *
 * Mappers stack, so that if multiple mappers return different names, then they
 * are all attempted in turn. This allows for instance, snake_case and kebab-case
 * to be mixed in the same project.
 */
interface ParameterMapper {
  fun map(param: String): String
}

object DefaultParamMapper : ParameterMapper {
  override fun map(param: String): String = param
}

fun Char.isUpperCase() = this == this.toUpperCase()

/**
 * A [ParameterMapper] that will transform a camel case name into
 * the snake case equivalent.
 *
 * For example, camelCasePilsen will become snake_case_pilsen.
 */
object SnakeCaseParamMapper : ParameterMapper {

  override fun map(param: String): String {
    return param.fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.toLowerCase().toString()
        char.isUpperCase() -> acc + "_" + char.toLowerCase()
        else -> acc + char
      }
    }
  }
}

/**
 * A [ParameterMapper] that will transform a camel case name into
 * the kebab case equivalent.
 *
 * For example, camelCasePilsen will become kebab_case_pilsen.
 */
object KebabCaseParamMapper : ParameterMapper {

  override fun map(param: String): String {
    return (param).fold("") { acc, char ->
      when {
        char.isUpperCase() && acc.isEmpty() -> char.toLowerCase().toString()
        char.isUpperCase() -> acc + "-" + char.toLowerCase()
        else -> acc + char
      }
    }
  }

}
