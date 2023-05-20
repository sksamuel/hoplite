package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.Parser
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigOrigin
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import java.io.InputStream
import java.io.InputStreamReader

class HoconParser : Parser {

  override fun load(input: InputStream, source: String): Node {
    val config = ConfigFactory.parseReader(InputStreamReader(input)).resolve()
    return MapProduction(config.root(), config.origin(), source, DotPath.root)
  }

  override fun defaultFileExtensions(): List<String> = listOf("conf")
}

fun ConfigOrigin.toPos(source: String): Pos = Pos.LinePos(this.lineNumber(), source)

object MapProduction {
  operator fun invoke(
    config: ConfigObject,
    origin: ConfigOrigin,
    source: String,
    path: DotPath
  ): Node {
    val obj = mutableMapOf<String, Node>()
    config.entries.forEach {
      val value = ValueProduction(it.value, source, path.with(it.key))
      obj[it.key] = value
    }
    return MapNode(obj, origin.toPos(source), path)
  }
}

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
object ValueProduction {
  operator fun invoke(value: ConfigValue, source: String, path: DotPath): Node {
    return when (value.valueType()) {
      ConfigValueType.OBJECT -> MapProduction(value as ConfigObject, value.origin(), source, path)
      ConfigValueType.NUMBER -> when (val v = value.unwrapped()) {
        is Double -> DoubleNode(v, value.origin().toPos(source), path, emptyMap())
        is Float -> DoubleNode(v.toDouble(), value.origin().toPos(source), path, emptyMap())
        is Long -> LongNode(v, value.origin().toPos(source), path, emptyMap())
        is Int -> LongNode(v.toLong(), value.origin().toPos(source), path, emptyMap())
        else -> throw RuntimeException("Unexpected element type for ConfigValueType.NUMBER: $v")
      }
      ConfigValueType.LIST -> ListProduction(value as ConfigList, value.origin(), source, path)
      ConfigValueType.BOOLEAN ->
        when (val v = value.unwrapped()) {
          is Boolean -> BooleanNode(v, value.origin().toPos(source), path, emptyMap())
          else -> throw RuntimeException("Unexpected element type for ConfigValueType.BOOLEAN: $v")
        }
      ConfigValueType.STRING -> StringNode(value.unwrapped().toString(), value.origin().toPos(source), path, emptyMap())
      ConfigValueType.NULL -> NullNode(value.origin().toPos(source), path, emptyMap())
    }
  }
}

object ListProduction {
  operator fun invoke(config: ConfigList, origin: ConfigOrigin, source: String, path: DotPath): ArrayNode {
    val elements = (0 until config.size).map {
      ValueProduction(config[it], source, path)
    }
    return ArrayNode(elements, origin.toPos(source), path)
  }
}
