package com.sksamuel.hoplite

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import java.net.URL

typealias ConfigResult<A> = ValidatedNel<ConfigFailure, A>

object ConfigResults {
  fun failed(description: String): ConfigResult<Nothing> = ConfigFailure(description).invalidNel()
}

/**
 * The file system location of a ConfigValue, represented by a url and a line
 * number
 *
 * @param url the URL describing the origin of the ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
data class ConfigLocation(val url: URL, val lineNumber: Int) {
  val description: String = "($url:$lineNumber)"
}
