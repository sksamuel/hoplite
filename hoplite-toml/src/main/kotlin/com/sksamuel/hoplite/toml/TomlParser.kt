package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.parsers.Parser
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.io.InputStream

class TomlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("toml")
  override fun load(input: InputStream, source: String): Node {
    val result = Toml.parse(input)
    return TableProduction(result, Pos.NoPos, "<root>", source)
  }
}

object TableProduction {
  operator fun invoke(table: TomlTable, pos: Pos, path: String, source: String): MapNode {
    val obj = mutableMapOf<String, Node>()
    for (key in table.keySet()) {
      val fieldPos = table.toPos(key, source)
      val fieldPath = "$path.$key"
      val value = when {
        table.isBoolean(key) -> BooleanNode(table.getBoolean(key)!!, fieldPos, fieldPath)
        table.isDouble(key) -> DoubleNode(table.getDouble(key)!!, fieldPos, fieldPath)
        table.isLong(key) -> LongNode(table.getLong(key)!!, fieldPos, fieldPath)
        table.isString(key) -> StringNode(table.getString(key)!!, fieldPos, fieldPath)
        table.isArray(key) -> ListProduction(table.getArray(key)!!, fieldPos, fieldPath, source)
        table.isTable(key) -> TableProduction(table.getTable(key)!!, fieldPos, fieldPath, source)
        else -> StringNode(table.get(key).toString(), fieldPos, fieldPath)
      }
      obj[key] = value
    }
    return MapNode(obj, pos, path)
  }
}

object ListProduction {
  operator fun invoke(array: TomlArray, pos: Pos, path: String, source: String): ListNode {
    val elements = (0 until array.size()).map { k ->
      when {
        array.containsBooleans() -> BooleanNode(array.getBoolean(k), pos, path)
        array.containsDoubles() -> DoubleNode(array.getDouble(k), pos, path)
        array.containsLongs() -> LongNode(array.getLong(k), pos, path)
        array.containsStrings() -> StringNode(array.getString(k), pos, path)
        array.containsArrays() -> ListProduction(array.getArray(k), pos, path, source)
        array.containsTables() -> TableProduction(array.getTable(k), pos, path, source)
        else -> StringNode(array[k].toString(), pos, path)
      }
    }
    return ListNode(elements, pos, path)
  }
}

fun TomlTable.toPos(key: String, source: String): Pos = inputPositionOf(key)?.let {
  Pos.LineColPos(it.line(), it.column(), source)
} ?: Pos.NoPos

