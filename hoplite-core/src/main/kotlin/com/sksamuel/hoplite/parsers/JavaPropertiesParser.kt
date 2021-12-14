package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.Path
import com.sksamuel.hoplite.Props
import com.sksamuel.hoplite.Value
import java.io.InputStream
import java.util.Properties

class JavaPropertiesParser : Parser {

  override fun load(input: InputStream, source: String): Props {
    val props = Properties()
    props.load(input)
    return object : Props {
      override fun at(path: Path): Value {
        TODO("Not yet implemented")
      }
    }
  }

  override fun defaultFileExtensions(): List<String> = listOf("props", "properties")
}
