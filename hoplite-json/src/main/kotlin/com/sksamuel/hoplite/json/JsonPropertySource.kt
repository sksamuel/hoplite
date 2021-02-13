package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid

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
  override fun node(context: PropertySourceContext): ConfigResult<Node> = JsonParser().load(str.byteInputStream(), "").valid()
}
