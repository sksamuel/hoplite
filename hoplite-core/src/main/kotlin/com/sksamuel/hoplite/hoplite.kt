package com.sksamuel.hoplite

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import kotlin.reflect.KType

typealias ConfigResult<A> = ValidatedNel<ConfigFailure, A>

object ConfigResults {
  fun NoSuchDecoder(type: KType): ConfigResult<Nothing> = ConfigFailure("No such decoder for $type").invalidNel()
  fun decodeFailure(node: Node, target: Class<*>?): ConfigResult<Nothing> = TODO()
  fun decodeFailure(node: Node, error: String): ConfigResult<Nothing> = TODO()
  fun failed(description: String): ConfigResult<Nothing> = ConfigFailure(description).invalidNel()
  fun failedTypeConversion(node: Node): ConfigResult<Nothing> = ConfigFailure("type conversion failure at $node").invalidNel()
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
