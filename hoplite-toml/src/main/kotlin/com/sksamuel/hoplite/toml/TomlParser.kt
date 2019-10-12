package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.parsers.Parser
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.io.InputStream

class TomlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("toml")
  override fun load(input: InputStream, source: String): TreeNode {
    val result = Toml.parse(input)
    return TableProduction(result, Pos.NoPos, source)
  }
}

object TableProduction {
  operator fun invoke(table: TomlTable, pos: Pos, source: String): TreeNode {
    val obj = mutableMapOf<String, TreeNode>()
    for (key in table.keySet()) {
      val fieldPos = table.toPos(key, source)
      val value = when {
        table.isBoolean(key) -> PrimitiveNode(Value.BooleanNode(table.getBoolean(key)!!), fieldPos)
        table.isDouble(key) -> PrimitiveNode(Value.DoubleNode(table.getDouble(key)!!), fieldPos)
        table.isLong(key) -> PrimitiveNode(Value.LongNode(table.getLong(key)!!), fieldPos)
        table.isString(key) -> PrimitiveNode(Value.StringNode(table.getString(key)!!), fieldPos)
        table.isArray(key) -> ListProduction(table.getArray(key)!!, fieldPos, source)
        table.isTable(key) -> TableProduction(table.getTable(key)!!, fieldPos, source)
        else ->  PrimitiveNode(Value.StringNode(table.get(key).toString()), fieldPos)
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
        array.containsBooleans() ->  PrimitiveNode(Value.BooleanNode(array.getBoolean(k)), pos)
        array.containsDoubles() ->  PrimitiveNode(Value.DoubleNode(array.getDouble(k)), pos)
        array.containsLongs() ->  PrimitiveNode(Value.LongNode(array.getLong(k)), pos)
        array.containsStrings() ->  PrimitiveNode(Value.StringNode(array.getString(k)), pos)
        array.containsArrays() -> ListProduction(array.getArray(k), pos, source)
        array.containsTables() -> TableProduction(array.getTable(k), pos, source)
        else ->  PrimitiveNode(Value.StringNode(array[k].toString()), pos)
      }
    }
    return ArrayNode(elements, pos)
  }
}

fun TomlTable.toPos(key: String, source: String): Pos = inputPositionOf(key)?.let {
  Pos.LineColPos(it.line(), it.column(), source)
} ?: Pos.NoPos

