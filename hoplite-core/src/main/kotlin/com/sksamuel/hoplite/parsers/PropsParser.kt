package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.TreeNode
import java.io.InputStream
import java.util.*

class PropsParser : Parser {

  override fun load(input: InputStream, source: String): TreeNode {
    val props = Properties()
    props.load(input)
    return loadProps(props, source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("props", "properties")
}
