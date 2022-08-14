package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import java.util.concurrent.atomic.AtomicInteger

private val counter = AtomicInteger(0)

/**
 * A [PropertySource] that allows providing JSON directly.
 *
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
  private val str: String,
  private val source: String = "Provided JSON ${counter.incrementAndGet()}",
) : PropertySource {

  override fun source(): String = source

  override fun node(context: PropertySourceContext): ConfigResult<Node> =
    JsonParser().load(str.byteInputStream(), source).valid()
}
