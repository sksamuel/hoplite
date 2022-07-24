package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import java.io.InputStream
import java.util.Properties

class PropsParser : Parser {

  override fun load(input: InputStream, source: String): Node {
    val props = Properties()
    props.load(input)
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
