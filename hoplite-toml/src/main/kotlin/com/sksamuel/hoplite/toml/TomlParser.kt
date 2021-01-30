package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.io.InputStream

class TomlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("toml")
  override fun load(input: InputStream, source: String): Node {
    val result = Toml.parse(input)
    return TableProduction(result, Pos.NoPos, source)
  }
}

/**
 * A [PropertySource] that provides values via a given toml string.
 * Eg, TomlPropertySource("""{"name":"sam"}""", "json")
 */
class TomlPropertySource(
  private val str: String
) : PropertySource {
  override fun node(parsers: ParserRegistry): ConfigResult<Node> = TomlParser().load(str.byteInputStream(), "").valid()
}

object TableProduction {
  operator fun invoke(table: TomlTable, pos: Pos, source: String): Node {
    val obj = mutableMapOf<String, Node>()
    for (key in table.keySet()) {
      val fieldPos = table.toPos(key, source)
      val value = when {
        table.isBoolean(key) -> BooleanNode(table.getBoolean(key)!!, fieldPos)
        table.isDouble(key) -> DoubleNode(table.getDouble(key)!!, fieldPos)
        table.isLong(key) -> LongNode(table.getLong(key)!!, fieldPos)
        table.isString(key) -> StringNode(table.getString(key)!!, fieldPos)
        table.isArray(key) -> ListProduction(table.getArray(key)!!, fieldPos, source)
        table.isTable(key) -> TableProduction(table.getTable(key)!!, fieldPos, source)
        else -> StringNode(table.get(key).toString(), fieldPos)
      }
      obj[key] = value
    }

    return MapNode(obj, pos)
  }
}

object ListProduction {
  operator fun invoke(array: TomlArray, pos: Pos, source: String): ArrayNode {
    val elements = (0 until array.size()).map { k ->
      when {
        array.containsBooleans() -> BooleanNode(array.getBoolean(k), pos)
        array.containsDoubles() -> DoubleNode(array.getDouble(k), pos)
        array.containsLongs() -> LongNode(array.getLong(k), pos)
        array.containsStrings() -> StringNode(array.getString(k), pos)
        array.containsArrays() -> ListProduction(array.getArray(k), pos, source)
        array.containsTables() -> TableProduction(array.getTable(k), pos, source)
        else -> StringNode(array[k].toString(), pos)
      }
    }
    return ArrayNode(elements, pos)
  }
}

fun TomlTable.toPos(key: String, source: String): Pos = inputPositionOf(key)?.let {
  Pos.LineColPos(it.line(), it.column(), source)
} ?: Pos.NoPos

