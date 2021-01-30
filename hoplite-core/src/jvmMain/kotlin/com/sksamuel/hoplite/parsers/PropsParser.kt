package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.Node
import java.io.ByteArrayInputStream
import java.util.Properties

class PropsParser : Parser {

  override fun load(bytes: ByteArray, source: String): Node {
    val props = Properties()
    props.load(ByteArrayInputStream(bytes))
    return props.toNode(source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("props", "properties")
}
