package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LookupPropertySource
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DataClassDecoder
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.decoder.InlineClassDecoder
import com.sksamuel.hoplite.decoder.LinkedHashMapDecoder
import com.sksamuel.hoplite.decoder.ListDecoder
import com.sksamuel.hoplite.decoder.MapDecoder
import com.sksamuel.hoplite.decoder.PairDecoder
import com.sksamuel.hoplite.decoder.PropsDecoder
import com.sksamuel.hoplite.decoder.SealedClassDecoder
import com.sksamuel.hoplite.decoder.SetDecoder
import com.sksamuel.hoplite.decoder.SortedSetDecoder
import com.sksamuel.hoplite.decoder.TripleDecoder
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.isDefined
import com.sksamuel.hoplite.withPath
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

/**
 * Builds the config tree a [LookupPropertySource] provides for one bind (gh-606): walks the target type from the
 * bind's prefix, asks the source for the paths the type needs, and returns the values found as a tree, keyed so
 * that the decoders find them where they would find a tree-based source's values.
 *
 * Each place in the walk has a list of candidate paths in the source, tried in order:
 *
 *  - For a data class parameter, the configured [com.sksamuel.hoplite.ParameterMapper]s give its names, and each name
 *    is one path element. The source decides the one key a path is read from, see [LookupPropertySource.key]: for
 *    environment variables, `db.pool.maxPoolSize` is read from `DB_POOL_MAXPOOLSIZE` and nothing else. Names that
 *    give the same key are tried once, as the first of them.
 *  - Where the binder needs to know which keys exist (map keys, list indices, and types whose decoder it can't
 *    see into, such as `Any` or custom decoders), it asks [LookupPropertySource.keysUnder].
 *  - A map key is the one place where a path isn't fixed by the type: the source's keys are split on its
 *    delimiter, so `LABELS_MY_KEY` may be the key `MY` with a value under `KEY`, or the key `MY_KEY`. Every run of
 *    elements under the map is tried as a key, shortest first, and each source key goes to the first map key that
 *    reads it, see [walkMap].
 */
internal class LookupTreeBuilder(
  private val source: LookupPropertySource,
  private val context: DecoderContext,
) {

  /**
   * A candidate path in the source, as path elements.
   */
  data class Candidate(val path: List<String>)

  /**
   * A place in the walk: where values are put in the resulting tree ([placed], as tree keys), and the candidate
   * paths in the source to read them from, in order.
   */
  private data class Location(
    val placed: List<String>,
    val candidates: List<Candidate>,
    // whether a single value at this location may be the value of the whole type
    val single: Boolean = true,
  )

  private class Element {
    var value: Node? = null
    var sourceKey: String? = null
    val children: MutableMap<String, Element> = linkedMapOf()
  }

  private val root = Element()
  private var pos: Pos = Pos.NoPos

  /**
   * Returns the values this source provides for [type] bound at [prefix], as a tree rooted at the config root,
   * or [Undefined] if the source has none.
   */
  fun build(type: KType, prefix: String?): Node {
    val elements = prefix?.split('.')?.filter { it.isNotEmpty() }.orEmpty()
    walk(type, Location(elements, listOf(Candidate(elements))), emptyList(), 0)
    return toNode(root, DotPath.root)
  }

  private fun walk(type: KType, location: Location, stack: List<KClass<*>>, depth: Int) {
    if (depth > MAX_DEPTH) return

    // any type may be given as a single value: a scalar, a comma separated list, a sealed subtype's name, ...
    if (location.single) lookupFirst(location)

    val decoder = decoder(type) ?: return importAll(location, depth)
    val kclass = type.classifier as KClass<*>

    when (decoder) {
      is DataClassDecoder -> walkDataClass(kclass, location, stack, depth)
      is SealedClassDecoder -> walkSealed(kclass, location, stack, depth)
      is MapDecoder, is LinkedHashMapDecoder -> type.arguments.getOrNull(1)?.type
        ?.let { walkMap(it, location, stack, depth) }
        ?: importAll(location, depth)
      is ListDecoder, is SetDecoder, is SortedSetDecoder -> type.arguments.getOrNull(0)?.type
        ?.let { walkCollection(it, location, stack, depth) }
        ?: importAll(location, depth)
      is InlineClassDecoder -> kclass.primaryConstructor?.parameters?.singleOrNull()
        ?.let { walk(it.type, location, stack, depth + 1) }
      else -> if (!decoder.isBuiltInScalar()) importAll(location, depth)
    }
  }

  /**
   * Returns the decoder the walk follows for [type], or null if it can't see into the type, which it then imports
   * in full.
   */
  private fun decoder(type: KType): Decoder<*>? {
    val kclass = type.classifier as? KClass<*> ?: return null
    if (kclass == Any::class) return null
    return context.decoders.decoder(kclass.starProjectedType).getOrElse { null }
  }

  private fun isSingleValue(type: KType): Boolean = decoder(type)?.isBuiltInScalar() ?: false

  /**
   * A decoder from hoplite core that reads a single value. Other decoders, including those of other modules and
   * custom ones, may read a whole subtree, so the binder imports everything under their path.
   */
  private fun Decoder<*>.isBuiltInScalar(): Boolean =
    this::class.java.`package`?.name == DataClassDecoder::class.java.`package`.name &&
      this !is PairDecoder && this !is TripleDecoder && this !is PropsDecoder && !isStructural()

  private fun Decoder<*>.isStructural(): Boolean = when (this) {
    is DataClassDecoder, is SealedClassDecoder, is InlineClassDecoder,
    is MapDecoder, is LinkedHashMapDecoder, is ListDecoder, is SetDecoder, is SortedSetDecoder -> true
    else -> false
  }

  private fun walkDataClass(kclass: KClass<*>, location: Location, stack: List<KClass<*>>, depth: Int) {
    // a recursive type only recurses while the source has keys there
    if (kclass in stack && location.candidates.none { source.keysUnder(it.path).isNotEmpty() }) return
    kclass.constructors.forEach { constructor ->
      constructor.parameters.forEach { param ->
        paramLocations(param, constructor, kclass, location).forEach {
          walk(param.type, it, stack + kclass, depth + 1)
        }
      }
    }
  }

  /**
   * Returns a location for each tree key the decoder looks [param] up by. The decoder finds a value under a name
   * either as is or as the node transformers spell it, so names the transformers spell the same go to one location,
   * under the first of them.
   */
  @Suppress("UNCHECKED_CAST")
  private fun paramLocations(
    param: KParameter,
    constructor: KFunction<*>,
    kclass: KClass<*>,
    location: Location,
  ): List<Location> {
    val names = context.paramMappers
      .flatMap { it.map(param, constructor as KFunction<Any>, kclass) }
      .distinct()
    return names.groupBy { transform(it) }.values.map { group ->
      val candidates = location.candidates.flatMap { candidate -> group.map { Candidate(candidate.path + it) } }
      // names that differ only in ways the source ignores, such as case, give the same key, so are read once
      Location(location.placed + group.first(), candidates.distinctBy { source.key(it.path) })
    }
  }

  private fun walkSealed(kclass: KClass<*>, location: Location, stack: List<KClass<*>>, depth: Int) {
    context.sealedTypeDiscriminatorField?.let { field ->
      lookupFirst(Location(location.placed + field, location.candidates.map { Candidate(it.path + field) }))
    }
    // the subtype is only known once the values are cascaded, so look up the values of every subtype
    kclass.sealedSubclasses.forEach { subclass ->
      if (subclass.objectInstance == null) walk(subclass.createType(), location, stack, depth + 1)
    }
  }

  // the places walked as maps, whose keys are never list indices
  private val mapLocations = mutableSetOf<List<String>>()

  private fun walkMap(valueType: KType, location: Location, stack: List<KClass<*>>, depth: Int) {
    mapLocations.add(location.placed)
    // the walk can't tell which keys a value it can't see into has, so the map is imported as the tree source would
    if (decoder(valueType) == null) return importAll(location, depth)
    val singleValue = isSingleValue(valueType)
    // A map key may contain the source's delimiter, e.g. `LABELS_MY_KEY` may be the key `MY_KEY` in `labels`, so
    // every run of path elements under the map is a possible key. Shorter keys are tried first, and a key is kept
    // only if it reads a value no shorter key read, so each value goes to one entry.
    val read = mutableSetOf<String?>()
    location.candidates
      .flatMap { candidate -> mapKeys(candidate.path, emptyList(), depth).map { candidate to it } }
      .sortedBy { (_, key) -> key.size }
      .forEach { (candidate, key) ->
        val path = candidate.path + key
        val placed = location.placed + key.joinToString(source.delimiter)
        // A value made of several keys can't be one value under a key of several path elements: given
        // `SERVERS_MY_SERVER_HOST`, `MY_SERVER_HOST` isn't a key whose value is a server.
        val single = key.size == 1 || singleValue
        val found = capture { walk(valueType, Location(placed, listOf(Candidate(path)), single), stack, depth + 1) }
        if (found.any { (_, node) -> node.sourceKey !in read }) {
          sourceKeyOf(placed, path)
          found.forEach { (placed, node) -> put(placed, node) }
          found.mapTo(read) { (_, node) -> node.sourceKey }
        }
      }
  }

  private fun mapKeys(path: List<String>, key: List<String>, depth: Int): List<List<String>> {
    if (depth > MAX_DEPTH || key.size >= MAX_MAP_KEY_ELEMENTS) return emptyList()
    return source.keysUnder(path + key).flatMap { element ->
      val longer = key + element
      listOf(longer) + mapKeys(path, longer, depth + 1)
    }
  }

  private fun walkCollection(elementType: KType, location: Location, stack: List<KClass<*>>, depth: Int) {
    location.candidates.forEach { candidate ->
      source.keysUnder(candidate.path).filter { it.toIntOrNull() != null }.forEach { index ->
        val path = candidate.path + index
        walk(elementType, Location(location.placed + index, listOf(Candidate(path))), stack, depth + 1)
      }
    }
  }

  /**
   * Imports everything under [location], split into path elements as the source splits them, as the tree-based
   * source would.
   */
  private fun importAll(location: Location, depth: Int) {
    location.candidates.forEach { candidate -> importAll(candidate.path, location.placed, depth) }
  }

  private fun importAll(path: List<String>, placed: List<String>, depth: Int) {
    if (depth > MAX_DEPTH) return
    source.lookup(path).takeIf { it.isDefined }?.let { put(placed, it) }
    source.keysUnder(path).forEach { importAll(path + it, placed + it, depth + 1) }
  }

  private fun lookupFirst(location: Location) {
    if (location.placed.isEmpty()) return
    location.candidates.asSequence()
      .map { source.lookup(it.path) }
      .firstOrNull { it.isDefined }
      ?.let { put(location.placed, it) }
  }

  private fun element(placed: List<String>): Element =
    placed.fold(root) { element, key -> element.children.getOrPut(key) { Element() } }

  // values found while trying a map key, held until it is known whether the key is kept
  private val captures = ArrayDeque<MutableList<Pair<List<String>, Node>>>()

  private fun capture(f: () -> Unit): List<Pair<List<String>, Node>> {
    captures.addLast(mutableListOf())
    f()
    return captures.removeLast()
  }

  private fun put(placed: List<String>, node: Node) {
    captures.lastOrNull()?.let { it.add(placed to node); return }
    if (pos == Pos.NoPos) pos = node.pos
    val element = element(placed)
    // the first value found for a place wins, as candidates are tried in order
    if (element.value == null) element.value = node
  }

  // records the source's key for a map entry, so the decoder can restore the map key as spelled in the source
  private val sourceKeys = mutableMapOf<List<String>, String>()

  private fun sourceKeyOf(placed: List<String>, path: List<String>) {
    sourceKeys.putIfAbsent(placed, path.joinToString(source.delimiter))
  }

  private fun toNode(element: Element, path: DotPath): Node {
    val value = element.value?.withPathDeep(path)
    if (element.children.isEmpty()) return value ?: Undefined
    val children = element.children
      .mapValues { (key, child) -> toNode(child, path.with(key)) }
      .filterValues { it.isDefined }
    if (children.isEmpty()) return value ?: Undefined
    val sourceKey = sourceKeys[path.keys] ?: value?.sourceKey ?: path.flatten(source.delimiter)
    // Indices only, so a list, as the tree-based environment variable source makes it (but not a map's keys). The
    // list stays a map keyed by index through the cascade, so the indices the source gave are kept, and becomes an
    // array after it, see [compactLists].
    val meta = if (path.keys !in mapLocations && children.keys.all { it.toIntOrNull() != null }) {
      mapOf(LIST_INDICES to true)
    } else emptyMap()
    return MapNode(children, pos, path, value ?: Undefined, meta, source.delimiter, sourceKey)
  }

  private fun Node.withPathDeep(path: DotPath): Node = when (this) {
    is MapNode -> copy(path = path, map = map.mapValues { (k, v) -> v.withPathDeep(path.with(k)) }, value = value.withPathDeep(path))
    is ArrayNode -> copy(path = path, elements = elements.mapIndexed { i, v -> v.withPathDeep(path.with(i.toString())) })
    else -> withPath(path)
  }

  private fun transform(name: String): String =
    context.nodeTransformers.fold(name) { n, transformer -> transformer.transformPathElement(n) }

  companion object {
    /**
     * Metadata on a [MapNode] whose keys are list indices, which [compactLists] turns into an [ArrayNode].
     */
    internal const val LIST_INDICES = "LookupListIndices"

    /**
     * Turns each map keyed by list indices into an array of its values in index order, dropping missing indices
     * and any value of the map itself, as the tree-based environment variable source does. Done after the lookup
     * tree has been cascaded with the tree sources, so the cascade sees the indices.
     */
    internal fun compactLists(node: Node): Node = when (node) {
      is MapNode if node.meta[LIST_INDICES] == true && node.map.keys.all { it.toIntOrNull() != null } ->
        ArrayNode(
          node.map.entries.sortedBy { it.key.toInt() }.map { compactLists(it.value) },
          node.pos, node.path, node.meta - LIST_INDICES, node.delimiter, node.sourceKey,
        )
      is MapNode -> node.copy(map = node.map.mapValues { compactLists(it.value) }, value = compactLists(node.value))
      is ArrayNode -> node.copy(elements = node.elements.map { compactLists(it) })
      else -> node
    }

    private const val MAX_DEPTH = 32
    private const val MAX_MAP_KEY_ELEMENTS = 8
  }
}
