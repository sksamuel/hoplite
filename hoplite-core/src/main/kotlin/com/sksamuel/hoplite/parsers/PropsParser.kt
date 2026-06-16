package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Properties

class PropsParser : Parser {

  override fun load(input: InputStream, source: String): Node {
    val props = Properties()
    // The reader wraps `input` and must be closed so its decoder buffers are released.
    // The caller still owns `input` and may close it independently — InputStream.close() is
    // idempotent so the double-close is harmless.
    InputStreamReader(input, Charset.forName("UTF-8")).use { reader -> props.load(reader) }
    return props.toNode(source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("props", "properties")
}

class PropsPropertySource(val props: Properties, val name: String = "props") : PropertySource {

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return props.toNode(name).valid()
  }

  override fun source(): String = name
}
