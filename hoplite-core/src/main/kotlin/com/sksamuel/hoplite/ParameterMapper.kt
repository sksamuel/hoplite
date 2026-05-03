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
 * Acronyms are kept as a single segment, so URLConfig becomes url_config
 * and myURL becomes my_url (matching the Spring/Jackson convention).
 *
 * When using the [PathNormalizer] (which is enabled by default), this mapper is unnecessary.
 */
object SnakeCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val name = param.name ?: return emptySet()
    val snake = splitOnWordBoundaries(name).joinToString("_")
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
 * Acronyms are kept as a single segment, so URLConfig becomes url-config
 * and myURL becomes my-url (matching the Spring/Jackson convention).
 *
 * When using the [PathNormalizer] (which is enabled by default), this mapper is unnecessary.
 */
object KebabCaseParamMapper : ParameterMapper {

  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val name = param.name ?: return emptySet()
    val kebab = splitOnWordBoundaries(name).joinToString("-")
    val trailingDigits = kebab.trailingDigits()
    val lastNumberVariant =
      if (trailingDigits.isEmpty()) null else kebab.removeSuffix(trailingDigits) + "-" + trailingDigits
    return setOfNotNull(kebab, lastNumberVariant)
  }
}

/**
 * Splits a camelCase / PascalCase identifier into its constituent words, lowercased. Acronyms
 * are kept as a single word: `URLConfig` -> `["url", "config"]`, `myURL` -> `["my", "url"]`,
 * `XMLHttpRequest` -> `["xml", "http", "request"]`. The previous implementation inserted a
 * separator before every uppercase letter, producing `u_r_l_config` from `URLConfig`.
 */
private fun splitOnWordBoundaries(name: String): List<String> {
  if (name.isEmpty()) return emptyList()
  val parts = mutableListOf<StringBuilder>()
  parts.add(StringBuilder())
  for (i in name.indices) {
    val c = name[i]
    val startsNewWord = i > 0 && c.isUpperCase() && (
      // boundary between lowercase/digit and uppercase: `myURL` -> `my|URL`
      !name[i - 1].isUpperCase() ||
      // boundary at the end of an acronym: `URLConfig` -> `URL|Config`
      (i + 1 < name.length && name[i + 1].isLowerCase())
    )
    if (startsNewWord) parts.add(StringBuilder())
    parts.last().append(c.lowercaseChar())
  }
  return parts.map { it.toString() }
}

private fun String.trailingDigits(): String {
  if (this.isEmpty()) return ""
  if (!this.last().isDigit()) return ""
  return dropLast(1).trailingDigits() + this.last()
}
