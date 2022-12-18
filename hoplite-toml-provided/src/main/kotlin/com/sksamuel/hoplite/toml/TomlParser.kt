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
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.Parser
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.io.InputStream

class TomlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("toml")
  override fun load(input: InputStream, source: String): Node {
    val result = Toml.parse(input)
    return TableProduction(result, Pos.SourcePos(source), source, DotPath.root)
  }
}

/**
 * A [PropertySource] that provides values via a given toml string.
 * Eg, TomlPropertySource("""{"name":"sam"}""", "json")
 */
class TomlPropertySource(
  private val str: String
) : PropertySource {

  override fun source(): String = "Provided TOML"

  override fun node(context: PropertySourceContext): ConfigResult<Node> =
    TomlParser().load(str.byteInputStream(), "").valid()
}

object TableProduction {
  operator fun invoke(table: TomlTable, pos: Pos, source: String, path: DotPath): Node {
    val obj = mutableMapOf<String, Node>()
    for (key in table.keySet()) {
      val fieldPos = table.toPos(key, source)
      // TomlJ will split the dots if we pass the key as-is, but at this point we know that our key is a *single
      // segment*. The wrapping is done to prevent TomlJ from parsing the key as a path. See #322 for more details.
      // For example, if key is hello.world, it means that our TOML contained a quoted "hello.world" key. But if we give
      // the key as-is to isXxx() functions, TomlJ will parse it as a "hello" -> "world" path. Wrapping it in a list
      // ensures that we always treat it as a single segment.
      val keyPath = listOf(key)
      val value = when {
        table.isBoolean(keyPath) -> BooleanNode(table.getBoolean(keyPath)!!, fieldPos, path.with(key), emptyMap())
        table.isDouble(keyPath) -> DoubleNode(table.getDouble(keyPath)!!, fieldPos, path.with(key), emptyMap())
        table.isLong(keyPath) -> LongNode(table.getLong(keyPath)!!, fieldPos, path.with(key), emptyMap())
        table.isString(keyPath) -> StringNode(table.getString(keyPath)!!, fieldPos, path.with(key), emptyMap())
        table.isArray(keyPath) -> ListProduction(table.getArray(keyPath)!!, fieldPos, source, path.with(key))
        table.isTable(keyPath) -> TableProduction(table.getTable(keyPath)!!, fieldPos, source, path.with(key))
        else -> StringNode(table.get(keyPath).toString(), fieldPos, path.with(key), emptyMap())
      }
      obj[key] = value
    }

    return MapNode(obj, pos, path)
  }
}

object ListProduction {
  operator fun invoke(array: TomlArray, pos: Pos, source: String, path: DotPath): ArrayNode {
    val elements = (0 until array.size()).map { k ->
      when (val value = array.get(k)) {
        is Boolean -> BooleanNode(value, pos, path, emptyMap())
        is Double -> DoubleNode(value, pos, path, emptyMap())
        is Long -> LongNode(value, pos, path, emptyMap())
        is String -> StringNode(value, pos, path, emptyMap())
        is TomlArray -> ListProduction(value, pos, source, path)
        is TomlTable -> TableProduction(value, pos, source, path)
        else -> StringNode(value.toString(), pos, path, emptyMap())
      }
    }
    return ArrayNode(elements, pos, path)
  }
}

fun TomlTable.toPos(key: String, source: String): Pos = inputPositionOf(key)?.let {
  Pos.LineColPos(it.line(), it.column(), source)
} ?: Pos.SourcePos(source)

