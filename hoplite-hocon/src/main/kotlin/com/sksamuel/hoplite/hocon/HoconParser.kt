package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
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

  override fun load(input: InputStream, source: String): Value {
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
                      source: String): Value {
    val obj = mutableMapOf<String, Value>()
    config.entries.forEach {
      val value = ValueProduction(it.value, "$path.${it.key}", source)
      obj[it.key] = value
    }
    return MapValue(obj, origin.toPos(source), path)
  }
}

object ValueProduction {
  operator fun invoke(value: ConfigValue, path: String, source: String): Value {
    return when (value.valueType()) {
      ConfigValueType.OBJECT -> MapProduction(value as ConfigObject, path, value.origin(), source)
      ConfigValueType.NUMBER -> when (val v = value.unwrapped()) {
        is Double -> DoubleValue(v, value.origin().toPos(source), path)
        is Float -> DoubleValue(v.toDouble(), value.origin().toPos(source), path)
        is Long -> LongValue(v, value.origin().toPos(source), path)
        is Int -> LongValue(v.toLong(), value.origin().toPos(source), path)
        else -> throw RuntimeException("Unexpected element type for ConfigValueType.NUMBER: $v")
      }
      ConfigValueType.LIST -> ListProduction(value as ConfigList, path, value.origin(), source)
      ConfigValueType.BOOLEAN ->
        when (val v = value.unwrapped()) {
          is Boolean -> BooleanValue(v, value.origin().toPos(source), path)
          else -> throw RuntimeException("Unexpected element type for ConfigValueType.BOOLEAN: $v")
        }
      ConfigValueType.STRING -> StringValue(value.unwrapped().toString(), value.origin().toPos(source), path)
      ConfigValueType.NULL -> NullValue(value.origin().toPos(source), path)
    }
  }
}

object ListProduction {
  operator fun invoke(config: ConfigList, path: String, origin: ConfigOrigin, source: String): ListValue {
    val elements = (0 until config.size).map {
      ValueProduction(config[it], "$path[$it]", source)
    }
    return ListValue(elements, origin.toPos(source), path)
  }
}
