package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import java.util.concurrent.atomic.AtomicInteger

private val counter = AtomicInteger(0)

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
  private val str: String,
  private val source: String = "Provided YAML ${counter.incrementAndGet()}",
) : PropertySource {

  override fun source(): String = source

  override fun node(context: PropertySourceContext): ConfigResult<Node> =
    YamlParser().load(str.byteInputStream(), source).valid()
}
