package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
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

  override fun load(input: InputStream, source: String): Node {
    val config = ConfigFactory.parseReader(InputStreamReader(input))
    return MapProduction(config.root(), "<root>", config.origin(), source)
  }

  override fun defaultFileExtensions(): List<String> = listOf("conf")
}

fun ConfigOrigin.toPos(source: String): Pos = Pos.LinePos(this.lineNumber(), source)

object MapProduction {
  operator fun invoke(config: ConfigObject,
                      path: String,
                      origin: ConfigOrigin,
                      source: String): Node {
    val obj = mutableMapOf<String, Node>()
    config.entries.forEach {
      val value = ValueProduction(it.value, "$path.${it.key}", source)
      obj[it.key] = value
    }
    return MapNode(obj, origin.toPos(source), path)
  }
}

object ValueProduction {
  operator fun invoke(value: ConfigValue, path: String, source: String): Node {
    return when (value.valueType()) {
      ConfigValueType.OBJECT -> MapProduction(value as ConfigObject, path, value.origin(), source)
      ConfigValueType.NUMBER -> when (val v = value.unwrapped()) {
        is Double -> DoubleNode(v, value.origin().toPos(source), path)
        is Float -> DoubleNode(v.toDouble(), value.origin().toPos(source), path)
        is Long -> LongNode(v, value.origin().toPos(source), path)
        is Int -> LongNode(v.toLong(), value.origin().toPos(source), path)
        else -> throw RuntimeException("Unexpected element type for ConfigValueType.NUMBER: $v")
      }
      ConfigValueType.LIST -> ListProduction(value as ConfigList, path, value.origin(), source)
      ConfigValueType.BOOLEAN ->
        when (val v = value.unwrapped()) {
          is Boolean -> BooleanNode(v, value.origin().toPos(source), path)
          else -> throw RuntimeException("Unexpected element type for ConfigValueType.BOOLEAN: $v")
        }
      ConfigValueType.STRING -> StringNode(value.unwrapped().toString(), value.origin().toPos(source), path)
      ConfigValueType.NULL -> NullNode(value.origin().toPos(source), path)
    }
  }
}

object ListProduction {
  operator fun invoke(config: ConfigList, path: String, origin: ConfigOrigin, source: String): ListNode {
    val elements = (0 until config.size).map {
      ValueProduction(config[it], "$path[$it]", source)
    }
    return ListNode(elements, origin.toPos(source), path)
  }
}
