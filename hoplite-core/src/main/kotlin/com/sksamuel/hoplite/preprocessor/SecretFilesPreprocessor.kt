@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.nio.file.NoSuchFileException
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
      // Surface a missing secret file as a clean ConfigFailure rather than letting
      // NoSuchFileException (or any other I/O failure) escape from the preprocessor and
      // crash the loader with a stack trace. The user typo'd or forgot to mount the secret
      // — they should see "secret 'x' not found at <path>" in the failure report.
      try {
        val replaced = regex.replace(node.value) { match ->
          val key = match.groupValues[1]
          basePath.resolve(key).readText().trimEnd('\r', '\n')
        }
        if (replaced == node.value) node.valid() else node.copy(value = replaced).valid()
      } catch (e: NoSuchFileException) {
        ConfigFailure.PreprocessorWarning("Secret file '${e.file}' not found").invalid()
      } catch (e: Exception) {
        ConfigFailure.PreprocessorFailure("Failed reading secret file from $basePath", e).invalid()
      }
    }
    else -> node.valid()
  }
}
