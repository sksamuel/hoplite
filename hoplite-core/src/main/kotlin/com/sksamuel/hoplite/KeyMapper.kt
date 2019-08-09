package com.sksamuel.hoplite

interface KeyMapper {
  fun map(key: String): String
}

fun defaultKeyMappers(): List<KeyMapper> = listOf(SnakeCaseKeyMapper, DashCaseKeyMapper)

/**
 * A [KeyMapper] that will transform any snake case field names
 * into their camel case equivalent.
 *
 * For example, snake_case_pilsen will become camelCasePilsen.
 *
 * This key mapper won't affect other camel case fields, so by using
 * this you can mix and match camel and snake case fields.
 */
object SnakeCaseKeyMapper : KeyMapper {

  private fun String.lowerFirst() = this[0].toLowerCase() + this.drop(1)
  private fun String.upperFirst() = this[0].toUpperCase() + this.drop(1)

  override fun map(key: String): String {
    val tokens = key.split('_')
    return tokens[0].lowerFirst() + tokens.drop(1).joinToString { it.upperFirst() }
  }
}

/**
 * A [KeyMapper] that will transform any dash case field names
 * into their camel case equivalent.
 *
 * For example, dash-case-pilsen will become camelCasePilsen.
 *
 * This key mapper won't affect other camel case fields, so by using
 * this you can mix and match camel and dash case fields.
 */
object DashCaseKeyMapper : KeyMapper {

  private fun String.lowerFirst() = this[0].toLowerCase() + this.drop(1)
  private fun String.upperFirst() = this[0].toUpperCase() + this.drop(1)

  override fun map(key: String): String {
    val tokens = key.split('-')
    return tokens[0].lowerFirst() + tokens.drop(1).joinToString { it.upperFirst() }
  }
}
