@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * Replaces strings of the form `${secret:key}` with the contents of the file at
 * `<basePath>/<key>`. A trailing line terminator (`\n` or `\r\n`) is stripped, so
 * editor- or shell-added newlines do not leak into config values.
 *
 * Useful for Docker / Kubernetes-style mounted secrets where each secret is a
 * separate file inside a directory.
 */
class SecretFilesPreprocessor(private val basePath: Path) : TraversingPrimitivePreprocessor() {

  constructor(basePath: String) : this(Path.of(basePath))

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{secret:(.+?)\\}".toRegex()

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      val replaced = regex.replace(node.value) { match ->
        val key = match.groupValues[1]
        basePath.resolve(key).readText().trimEnd('\r', '\n')
      }
      if (replaced == node.value) node.valid() else node.copy(value = replaced).valid()
    }
    else -> node.valid()
  }
}
