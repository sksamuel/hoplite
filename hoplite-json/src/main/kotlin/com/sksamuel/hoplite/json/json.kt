package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Parser
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import java.io.InputStream
import java.lang.UnsupportedOperationException

object Json : Parser {

  private val jsonFactory = JsonFactory()

  override fun load(input: InputStream): Value {
    val parser = jsonFactory.createParser(input)
    parser.nextToken()
    return TokenProduction.parse(parser)
  }
}

interface Production {
  fun parse(parser: JsonParser): Value
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object TokenProduction : Production {
  override fun parse(parser: JsonParser): Value {
    return when (parser.currentToken()) {
      JsonToken.NOT_AVAILABLE -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}")
      JsonToken.START_OBJECT -> ObjectProduction.parse(parser)
      JsonToken.START_ARRAY -> ArrayProduction.parse(parser)
      JsonToken.VALUE_EMBEDDED_OBJECT -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}")
      JsonToken.VALUE_STRING -> StringValue(parser.valueAsString, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_INT -> LongValue(parser.valueAsLong, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_FLOAT -> DoubleValue(parser.valueAsDouble, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_TRUE -> BooleanValue(true, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_FALSE -> BooleanValue(false, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NULL -> NullValue(parser.currentLocation.toLineColPos())
      else -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}; encountered unexpected token ${parser.currentToken}")
    }
  }
}

private fun JsonLocation.toLineColPos(): Pos = Pos.LineColPos(this.lineNr, this.columnNr)

object ObjectProduction : Production {
  override fun parse(parser: JsonParser): Value {
    require(parser.currentToken == JsonToken.START_OBJECT)
    val loc = parser.currentLocation
    val obj = mutableMapOf<String, Value>()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      require(parser.currentToken() == JsonToken.FIELD_NAME)
      val fieldName = parser.currentName()
      parser.nextToken()
      val value = TokenProduction.parse(parser)
      obj[fieldName] = value
    }
    return MapValue(obj, loc.toPos())
  }
}

fun JsonLocation.toPos(): Pos = Pos.LineColPos(this.lineNr, this.columnNr)

object ArrayProduction : Production {
  override fun parse(parser: JsonParser): Value {
    require(parser.currentToken == JsonToken.START_ARRAY)
    val loc = parser.currentLocation
    val list = mutableListOf<Value>()
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      val value = TokenProduction.parse(parser)
      list.add(value)
    }
    require(parser.currentToken == JsonToken.END_ARRAY)
    return ListValue(list.toList(), loc.toPos())
  }
}