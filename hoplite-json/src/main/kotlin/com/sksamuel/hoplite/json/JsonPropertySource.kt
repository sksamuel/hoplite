package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry

/**
 * A [PropertySource] that provides values via a given yaml string.
 * For example:
 *
 *   JsonParser(
 *      """ {
 *        "a": [
 *          {
 *            "key": "arnold",
 *            "value": "rimmer"
 *          },
 *          {
 *            "key": "mr",
 *            "value": "flibble"
 *          }
 *        ]
 *     } """)
 */
class JsonPropertySource(
  private val str: String
) : PropertySource {
  override fun node(parsers: ParserRegistry): ConfigResult<Node> = JsonParser().load(str.byteInputStream(), "").valid()
}
