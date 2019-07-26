package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Parser
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import java.io.InputStream
import java.lang.UnsupportedOperationException

object Json : Parser {

  private val jsonFactory = JsonFactory()

  override fun load(input: InputStream): Node {
    val parser = jsonFactory.createParser(input)
    parser.nextToken()
    return TokenProduction.parse(parser)
  }
}

interface Production {
  fun parse(parser: JsonParser): Node
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object TokenProduction : Production {
  override fun parse(parser: JsonParser): Node {
    return when (parser.currentToken()) {
      JsonToken.NOT_AVAILABLE -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}")
      JsonToken.START_OBJECT -> ObjectProduction.parse(parser)
      JsonToken.START_ARRAY -> ArrayProduction.parse(parser)
      JsonToken.VALUE_STRING -> StringNode(parser.valueAsString, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_INT -> LongNode(parser.valueAsLong, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NUMBER_FLOAT -> DoubleNode(parser.valueAsDouble, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_TRUE -> BooleanNode(true, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_FALSE -> BooleanNode(false, parser.currentLocation.toLineColPos())
      JsonToken.VALUE_NULL -> NullNode(parser.currentLocation.toLineColPos())
      else -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}; encountered unexpected token ${parser.currentToken}")
    }
  }
}

private fun JsonLocation.toLineColPos(): Pos = Pos.LineColPos(this.lineNr, this.columnNr)

object ObjectProduction : Production {
  override fun parse(parser: JsonParser): Node {
    require(parser.currentToken == JsonToken.START_OBJECT)
    val loc = parser.currentLocation
    val obj = mutableMapOf<String, Node>()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      require(parser.currentToken() == JsonToken.FIELD_NAME)
      val fieldName = parser.currentName()
      parser.nextToken()
      val value = TokenProduction.parse(parser)
      obj[fieldName] = value
    }
    return MapNode(obj, loc.toPos())
  }
}

fun JsonLocation.toPos(): Pos = Pos.LineColPos(this.lineNr, this.columnNr)

object ArrayProduction : Production {
  override fun parse(parser: JsonParser): Node {
    require(parser.currentToken == JsonToken.START_ARRAY)
    val loc = parser.currentLocation
    val list = mutableListOf<Node>()
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      val value = TokenProduction.parse(parser)
      list.add(value)
    }
    require(parser.currentToken == JsonToken.END_ARRAY)
    return ListNode(list.toList(), loc.toPos())
  }
}