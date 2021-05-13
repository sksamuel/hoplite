package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.parsers.Parser
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.events.AliasEvent
import org.yaml.snakeyaml.events.DocumentEndEvent
import org.yaml.snakeyaml.events.DocumentStartEvent
import org.yaml.snakeyaml.events.Event
import org.yaml.snakeyaml.events.MappingEndEvent
import org.yaml.snakeyaml.events.MappingStartEvent
import org.yaml.snakeyaml.events.ScalarEvent
import org.yaml.snakeyaml.events.SequenceEndEvent
import org.yaml.snakeyaml.events.SequenceStartEvent
import org.yaml.snakeyaml.events.StreamEndEvent
import org.yaml.snakeyaml.events.StreamStartEvent
import java.io.InputStream
import java.io.InputStreamReader

class YamlParser : Parser {
  override fun defaultFileExtensions(): List<String> = listOf("yml", "yaml")
  private val yaml = Yaml(SafeConstructor())
  override fun load(input: InputStream, source: String): Node {
    val reader = InputStreamReader(input)
    val events = yaml.parse(reader).iterator()
    val stream = TokenStream(events)
    require(stream.next().`is`(Event.ID.StreamStart)) { "Expected stream start at ${stream.current().startMark}" }
    require(stream.next().`is`(Event.ID.DocumentStart)) { "Expected document start at ${stream.current().startMark}" }
    stream.next()
    return TokenProduction(stream, source, emptyMap()).first
  }
}

interface TokenStream<T> {

  fun next(): T
  fun current(): T

  companion object {
    operator fun <T> invoke(iter: Iterator<T>) = object : TokenStream<T> {
      var current: T? = null
      override fun next(): T {
        current = iter.next()
        return current()
      }

      override fun current(): T = current ?: throw NoSuchElementException()
    }
  }
}

fun Event.id() = when (this) {
  is DocumentStartEvent -> Event.ID.DocumentStart
  is DocumentEndEvent -> Event.ID.DocumentEnd
  is MappingStartEvent -> Event.ID.MappingStart
  is MappingEndEvent -> Event.ID.MappingEnd
  is ScalarEvent -> Event.ID.Scalar
  is AliasEvent -> Event.ID.Alias
  is SequenceStartEvent -> Event.ID.SequenceStart
  is SequenceEndEvent -> Event.ID.SequenceEnd
  is StreamStartEvent -> Event.ID.StreamStart
  is StreamEndEvent -> Event.ID.StreamEnd
  else -> throw UnsupportedOperationException()
}

object TokenProduction {
  operator fun invoke(
    stream: TokenStream<Event>,
    source: String,
    anchors: Map<String, Node>,
  ): Pair<Node, Map<String, Node>> {
    return when (val event = stream.current()) {
      is MappingStartEvent -> MapProduction(stream, source, anchors)
      is SequenceStartEvent -> SequenceProduction(stream, source, anchors)
      // https://yaml.org/refcard.html
      // Language Independent Scalar types:
      //    { ~, null }              : Null (no value).
      //    [ 1234, 0x4D2, 02333 ]   : [ Decimal int, Hexadecimal int, Octal int ]
      //    [ 1_230.15, 12.3015e+02 ]: [ Fixed float, Exponential float ]
      //    [ .inf, -.Inf, .NAN ]    : [ Infinity (float), Negative, Not a number ]
      //    { Y, true, Yes, ON  }    : Boolean true
      //    { n, FALSE, No, off }    : Boolean false
      is ScalarEvent -> {
        val node = if (event.value == "null" && event.scalarStyle == DumperOptions.ScalarStyle.PLAIN)
          NullNode(event.startMark.toPos(source))
        else
          StringNode(event.value, event.startMark.toPos(source))
        if (event.anchor == null) Pair(node, anchors) else Pair(node, anchors.plus(event.anchor to node))
      }
      is AliasEvent -> {
        val node = anchors[event.anchor]
        if (node == null) error("Could not find alias ${event.anchor}") else Pair(node, anchors)
      }
      else -> throw java.lang.UnsupportedOperationException(
        "Invalid YAML event ${stream.current().id()} at ${stream.current().startMark}"
      )
    }
  }
}

object MapProduction {
  operator fun invoke(
    stream: TokenStream<Event>,
    source: String,
    anchors: Map<String, Node>
  ): Pair<Node, Map<String, Node>> {
    require(stream.current().`is`(Event.ID.MappingStart)) { "Expected mapping start at ${stream.current().startMark}" }
    val mapEvent = stream.current() as MappingStartEvent
    val obj = mutableMapOf<String, Node>()
    var tempAnchors: Map<String, Node> = anchors
    while (stream.next().id() != Event.ID.MappingEnd) {
      require(stream.current().id() == Event.ID.Scalar) { "Expected scalar at ${stream.current().startMark}" }
      val field = stream.current() as ScalarEvent
      val fieldName = field.value
      val anchor = field.anchor
      stream.next()
      val (node, returnedAnchors) = TokenProduction(stream, source, tempAnchors)
      tempAnchors = returnedAnchors
      if (anchor != null) tempAnchors = (tempAnchors + Pair(anchor, node))
      obj[fieldName] = node
    }
    require(stream.current().`is`(Event.ID.MappingEnd)) { "Expected mapping end at ${stream.current().startMark}" }
    val node = MapNode(obj.toMap(), mapEvent.startMark.toPos(source), Undefined)
    tempAnchors = when (val anchor = mapEvent.anchor) {
      null -> tempAnchors
      else -> tempAnchors + Pair(anchor, node)
    }
    return Pair(node, tempAnchors)
  }
}

object SequenceProduction {
  operator fun invoke(
    stream: TokenStream<Event>,
    source: String,
    anchors: Map<String, Node>
  ): Pair<Node, Map<String, Node>> {
    require(
      stream.current().`is`(Event.ID.SequenceStart)
    ) { "Expected sequence start at ${stream.current().startMark}" }
    val mark = stream.current().startMark
    val list = mutableListOf<Node>()
    var index = 0
    var tempAnchors: Map<String, Node> = anchors
    while (stream.next().id() != Event.ID.SequenceEnd) {
      val (node, returnedAnchors) = TokenProduction(stream, source, tempAnchors)
      list.add(node)
      index++
      tempAnchors = returnedAnchors
    }
    require(stream.current().`is`(Event.ID.SequenceEnd)) { "Expected sequence end at ${stream.current().startMark}" }
    return Pair(ArrayNode(list.toList(), mark.toPos(source)), tempAnchors)
  }
}

fun Mark.toPos(source: String): Pos = Pos.LineColPos(line, column, source)
