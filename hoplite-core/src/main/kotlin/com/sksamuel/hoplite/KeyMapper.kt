package com.sksamuel.hoplite

interface KeyMapper {
  fun map(key: String): String
}

fun defaultKeyMappers(): List<KeyMapper> = listOf(SnakeCaseKeyMapper, KebabCaseKeyMapper)

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



  override fun map(key: String): String {
    if (key.isBlank()) return key
    val tokens = key.split('_')
    return tokens[0].lowerFirst() + tokens.drop(1).joinToString("") { it.upperFirst() }
  }
}

fun String.lowerFirst() = if (this.isBlank()) "" else this[0].toLowerCase() + this.drop(1)
fun String.upperFirst() = if (this.isBlank()) "" else this[0].toUpperCase() + this.drop(1)

/**
 * A [KeyMapper] that will transform any dash case field names
 * into their camel case equivalent.
 *
 * For example, dash-case-pilsen will become camelCasePilsen.
 *
 * This key mapper won't affect other camel case fields, so by using
 * this you can mix and match camel and dash case fields.
 */
object KebabCaseKeyMapper : KeyMapper {

  override fun map(key: String): String {
    if (key.isBlank()) return key
    val tokens = key.split('-')
    return tokens[0].lowerFirst() + tokens.drop(1).joinToString("") { it.upperFirst() }
  }
}
