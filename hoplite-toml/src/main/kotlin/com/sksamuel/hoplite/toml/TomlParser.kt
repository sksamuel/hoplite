package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.Key
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.parsers.Parser
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.io.InputStream

class TomlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("toml")
  override fun load(input: InputStream, source: String): Map<Key, Value> {
    val result = Toml.parse(input)
    return TableProduction(result, Pos.NoPos, Key.root, source)
  }
}

object TableProduction {
  operator fun invoke(table: TomlTable, pos: Pos, path: Key, source: String): Map<Key, Value> {
    table.keySet().map { key ->
      val fieldPos = table.toPos(key, source)
      val value = when {
        table.isBoolean(key) -> BooleanValue(table.getBoolean(key)!!, fieldPos, fieldPath)
        table.isDouble(key) -> DoubleValue(table.getDouble(key)!!, fieldPos, fieldPath)
        table.isLong(key) -> LongValue(table.getLong(key)!!, fieldPos, fieldPath)
        table.isString(key) -> StringValue(table.getString(key)!!, fieldPos, fieldPath)
        table.isArray(key) -> ListProduction(table.getArray(key)!!, fieldPos, fieldPath, source)
        table.isTable(key) -> TableProduction(table.getTable(key)!!, fieldPos, fieldPath, source)
        else -> StringValue(table.get(path).toString(), fieldPos, fieldPath)
      }
    }

    val obj = mutableMapOf<String, Value>()
    for (key in table.keySet()) {
      val fieldPos = table.toPos(key, source)
      val fieldPath = "$path.$key"
      val value = when {
        table.isBoolean(key) -> BooleanValue(table.getBoolean(key)!!, fieldPos, fieldPath)
        table.isDouble(key) -> DoubleValue(table.getDouble(key)!!, fieldPos, fieldPath)
        table.isLong(key) -> LongValue(table.getLong(key)!!, fieldPos, fieldPath)
        table.isString(key) -> StringValue(table.getString(key)!!, fieldPos, fieldPath)
        table.isArray(key) -> ListProduction(table.getArray(key)!!, fieldPos, fieldPath, source)
        table.isTable(key) -> TableProduction(table.getTable(key)!!, fieldPos, fieldPath, source)
        else -> StringValue(table.get(key).toString(), fieldPos, fieldPath)
      }
      obj[key] = value
    }
    return MapValue(obj, pos, path)
  }
}

object ListProduction {
  operator fun invoke(array: TomlArray, pos: Pos, path: String, source: String): ListValue {
    val elements = (0 until array.size()).map { k ->
      when {
        array.containsBooleans() -> BooleanValue(array.getBoolean(k), pos, path)
        array.containsDoubles() -> DoubleValue(array.getDouble(k), pos, path)
        array.containsLongs() -> LongValue(array.getLong(k), pos, path)
        array.containsStrings() -> StringValue(array.getString(k), pos, path)
        array.containsArrays() -> ListProduction(array.getArray(k), pos, path, source)
        array.containsTables() -> TableProduction(array.getTable(k), pos, path, source)
        else -> StringValue(array[k].toString(), pos, path)
      }
    }
    return ListValue(elements, pos, path)
  }
}

fun TomlTable.toPos(key: String, source: String): Pos = inputPositionOf(key)?.let {
  Pos.LineColPos(it.line(), it.column(), source)
} ?: Pos.NoPos

