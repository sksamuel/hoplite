package com.sksamuel.hoplite

import arrow.data.ValidatedNel
import arrow.data.invalidNel

typealias ConfigResult<A> = ValidatedNel<ConfigFailure, A>

object ConfigResults {
  fun failed(description: String): ConfigResult<Nothing> = ConfigFailure(description).invalidNel()
  fun failedTypeConversion(value: Value): ConfigResult<Nothing> = ConfigFailure("type conversion failure at $value").invalidNel()
}

/**
 * The file system location of a ConfigValue, represented by a url and a line
 * number
 *
 * @param url the URL describing the origin of the ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
data class ConfigLocation(val resource: String, val pos: Pos) {
  val description: String = "($resource:${pos.line})"
}
