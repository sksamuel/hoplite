package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.parsers.Parser
import java.io.InputStream
import java.lang.UnsupportedOperationException

class JsonParser : Parser {

  private val jsonFactory = JsonFactory()

  override fun load(input: InputStream, source: String): TreeNode {
    val parser = jsonFactory.createParser(input).configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    parser.nextToken()
    return TokenProduction(parser, source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("json")
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object TokenProduction {
  operator fun invoke(parser: JsonParser, source: String): TreeNode {
    return when (parser.currentToken()) {
      JsonToken.NOT_AVAILABLE -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}")
      JsonToken.START_OBJECT -> ObjectProduction(parser, source)
      JsonToken.START_ARRAY -> ArrayProduction(parser, source)
      JsonToken.VALUE_STRING -> PrimitiveNode(Value.StringNode(parser.valueAsString),
        parser.currentLocation.toPos(source))
      JsonToken.VALUE_NUMBER_INT -> PrimitiveNode(Value.LongNode(parser.valueAsLong),
        parser.currentLocation.toPos(source))
      JsonToken.VALUE_NUMBER_FLOAT -> PrimitiveNode(Value.DoubleNode(parser.valueAsDouble),
        parser.currentLocation.toPos(source))
      JsonToken.VALUE_TRUE -> PrimitiveNode(Value.BooleanNode(true), parser.currentLocation.toPos(source))
      JsonToken.VALUE_FALSE -> PrimitiveNode(Value.BooleanNode(false), parser.currentLocation.toPos(source))
      JsonToken.VALUE_NULL -> PrimitiveNode(Value.NullValue, parser.currentLocation.toPos(source))
      else -> throw UnsupportedOperationException("Invalid json at ${parser.currentLocation}; encountered unexpected token ${parser.currentToken}")
    }
  }
}

fun JsonLocation.toPos(source: String): Pos = Pos.LineColPos(this.lineNr, this.columnNr, source)

object ObjectProduction {
  operator fun invoke(parser: JsonParser, source: String): TreeNode {
    require(parser.currentToken == JsonToken.START_OBJECT)
    val loc = parser.currentLocation
    val obj = mutableMapOf<String, TreeNode>()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      require(parser.currentToken() == JsonToken.FIELD_NAME)
      val fieldName = parser.currentName()
      parser.nextToken()
      val value = TokenProduction(parser, source)
      obj[fieldName] = value
    }
    return MapNode(obj, loc.toPos(source))
  }
}

object ArrayProduction {
  operator fun invoke(parser: JsonParser, source: String): TreeNode {
    require(parser.currentToken == JsonToken.START_ARRAY)
    val loc = parser.currentLocation
    val list = mutableListOf<TreeNode>()
    var index = 0
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      val value = TokenProduction(parser, source)
      list.add(value)
      index++
    }
    require(parser.currentToken == JsonToken.END_ARRAY)
    return ArrayNode(list.toList(), loc.toPos(source))
  }
}
