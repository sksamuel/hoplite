package com.sksamuel.hoplite

import kotlin.reflect.KParameter

/**
 * A [ParameterMapper] that will transform any snake case field names
 * into their camel case equivalent.
 *
 * For example, snake_case_pilsen will become camelCasePilsen.
 *
 * This key mapper won't affect other camel case fields, so by using
 * this you can mix and match camel and snake case fields.
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
 * A [ParameterMapper] that will transform any dash case field names
 * into their camel case equivalent.
 *
 * For example, dash-case-pilsen will become camelCasePilsen.
 *
 * This key mapper won't affect other camel case fields, so by using
 * this you can mix and match camel and dash case fields.
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
