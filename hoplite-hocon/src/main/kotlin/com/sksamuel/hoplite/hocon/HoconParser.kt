package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.parsers.Parser
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigOrigin
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException

class HoconParser : Parser {

  override fun load(input: InputStream, source: String): TreeNode {
    val config = ConfigFactory.parseReader(InputStreamReader(input))
    return MapProduction(config.root(), config.origin(), source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("conf")
}

fun ConfigOrigin.toPos(source: String): Pos = Pos.LinePos(this.lineNumber(), source)

object MapProduction {
  operator fun invoke(config: ConfigObject,
                      origin: ConfigOrigin,
                      source: String): TreeNode {
    val obj = mutableMapOf<String, TreeNode>()
    config.entries.forEach {
      val value = ValueProduction(it.value, source)
      obj[it.key] = value
    }
    return MapNode(obj, origin.toPos(source))
  }
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object ValueProduction {
  operator fun invoke(value: ConfigValue, source: String): TreeNode {
    return when (value.valueType()) {
      ConfigValueType.OBJECT -> MapProduction(value as ConfigObject, value.origin(), source)
      ConfigValueType.NUMBER -> when (val v = value.unwrapped()) {
        is Double -> PrimitiveNode(Value.DoubleNode(v), value.origin().toPos(source))
        is Float -> PrimitiveNode(Value.DoubleNode(v.toDouble()), value.origin().toPos(source))
        is Long ->PrimitiveNode( Value.LongNode(v), value.origin().toPos(source))
        is Int -> PrimitiveNode(Value.LongNode(v.toLong()), value.origin().toPos(source))
        else -> throw RuntimeException("Unexpected element type for ConfigValueType.NUMBER: $v")
      }
      ConfigValueType.LIST -> ListProduction(value as ConfigList, value.origin(), source)
      ConfigValueType.BOOLEAN ->
        when (val v = value.unwrapped()) {
          is Boolean -> PrimitiveNode(Value.BooleanNode(v), value.origin().toPos(source))
          else -> throw RuntimeException("Unexpected element type for ConfigValueType.BOOLEAN: $v")
        }
      ConfigValueType.STRING -> PrimitiveNode(Value.StringNode(value.unwrapped().toString()), value.origin().toPos(source))
      ConfigValueType.NULL -> PrimitiveNode(Value.NullValue, value.origin().toPos(source))
    }
  }
}

object ListProduction {
  operator fun invoke(config: ConfigList,origin: ConfigOrigin, source: String): ArrayNode {
    val elements = (0 until config.size).map {
      ValueProduction(config[it], source)
    }
    return ArrayNode(elements, origin.toPos(source))
  }
}
