package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken

fun main() {

  val json = """{ "name":"sam", "location":"chicago" }"""

  val jsonFactory = JsonFactory() // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
  val parser = jsonFactory.createParser(json) // or URL, Stream, Reader, String, byte[]
  while (parser.nextToken() !== JsonToken.END_OBJECT) {
    if (parser.currentToken == JsonToken.FIELD_NAME) {
      println(parser.tokenLocation.lineNr)
      println(parser.currentName)
      println(parser.text)
    }
  }
  parser.close()
}