package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import java.io.InputStream
import java.lang.UnsupportedOperationException

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

interface Parser {
  fun load(input: InputStream): Value
}

object JacksonParser : Parser {

  private val jsonFactory = JsonFactory()

  override fun load(input: InputStream): Value {
    val parser = jsonFactory.createParser(input)
    return TokenProduction.parse(parser)
  }
}

interface Production {
  fun parse(parser: JsonParser): Value
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object TokenProduction : Production {
  override fun parse(parser: JsonParser): Value {
    return when (parser.nextToken()) {
      JsonToken.NOT_AVAILABLE -> throw UnsupportedOperationException("Invalid json")
      JsonToken.START_OBJECT -> ObjectProduction.parse(parser)
      JsonToken.END_OBJECT -> TODO()
      JsonToken.START_ARRAY -> TODO()
      JsonToken.END_ARRAY -> TODO()
      JsonToken.FIELD_NAME -> TODO()
      JsonToken.VALUE_EMBEDDED_OBJECT -> TODO()
      JsonToken.VALUE_STRING -> StringValue(parser.valueAsString, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_INT -> LongValue(parser.valueAsLong, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_FLOAT -> DoubleValue(parser.valueAsDouble, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_TRUE -> BooleanValue(true, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_FALSE -> BooleanValue(false, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NULL -> NullValue(parser.currentLocation.toLineColPos())
    }
  }
}

private fun JsonLocation.toLineColPos(): Pos = Pos.LineColPos(this.lineNr, this.columnNr)

object ObjectProduction : Production {
  override fun parse(parser: JsonParser): Value {
    require(parser.currentToken == JsonToken.START_OBJECT)
    val start = parser.currentLocation.charOffset
    val obj = mutableMapOf<String, Value>()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      require(parser.currentToken() == JsonToken.FIELD_NAME)
      val fieldName = parser.currentName()
      val value = TokenProduction.parse(parser)
      obj[fieldName] = value
    }
    val end = parser.currentLocation.charOffset
    return MapValue(obj, Pos.RangePos(start, end))
  }
}