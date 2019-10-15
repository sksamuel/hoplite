package com.sksamuel.hoplite

import kotlin.reflect.KParameter

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
