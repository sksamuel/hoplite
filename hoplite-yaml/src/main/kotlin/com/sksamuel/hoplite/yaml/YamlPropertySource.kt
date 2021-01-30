package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry

/**
 * A [PropertySource] that provides values via a given yaml string.
 * For example:
 *
 *   YamlPropertySource(
 *      """
 *        a:
 *          name: Sam
 *        v:
 *          city: Chicago
 *      """)
 */
class YamlPropertySource(
  private val str: String
) : PropertySource {
  override fun node(parsers: ParserRegistry): ConfigResult<Node> = YamlParser().load(str.byteInputStream(), "").valid()
}
