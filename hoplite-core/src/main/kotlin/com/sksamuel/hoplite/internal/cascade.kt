package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

class Cascader(
  private val cascadeMode: CascadeMode,
  private val allowEmptyTree: Boolean,
  private val allowNullOverride: Boolean = false,
  private val sealedTypeDiscriminatorField: String? = null,
) {

  fun cascade(nodes: NonEmptyList<Node>): ConfigResult<Node> {

    val reduced = nodes.list.fold(CascadeResult(Undefined)) { a, b ->
      val result = cascade(a.node, b)
      CascadeResult(result.node, a.overrides + result.overrides)
    }

    return when {
      cascadeMode == CascadeMode.Error && reduced.overrides.isNotEmpty() ->
        ConfigFailure.OverrideConfigError(reduced.overrides).invalid()
      reduced.node == Undefined && allowEmptyTree -> MapNode(emptyMap(), Pos.NoPos, DotPath.root).valid()
      reduced.node == Undefined -> ConfigFailure.UndefinedTree.invalid()
      else -> reduced.node.valid()
    }
  }

  /**
   * Returns a new [Node] which is the result of merging node a with node b,
   * with keys in node a taking precedence if they are defined and not null.
   *
   * @return a [CascadeResult] containing the resolved [Node], and a list of overrides.
   */
  internal fun cascade(a: Node, b: Node): CascadeResult {

    if (b is Undefined) return CascadeResult(a)
    if (a is Undefined) return CascadeResult(b)

    require(a.path == b.path) {
      "Trying to merge node with path ${a.path.flatten()} with node with path ${b.path.flatten()}"
    }

    val isFallthrough = when (cascadeMode) {
      CascadeMode.Override, CascadeMode.Fallthrough -> true
      else -> false
    }

    return when (a) {
      // if I am null, then we should use the other one, unless allow null overriding is set, but that is not an override
      is NullNode -> if (allowNullOverride) CascadeResult(a) else CascadeResult(b)
      is MapNode -> when {
        // in fallthrough mode, this entire map takes precendence.
        // note override only happens after root, otherwise one entire file would override another
        isFallthrough && a.path != DotPath.root -> CascadeResult(a)
        else -> {
          // in explicit sealed types mode, only the higher priority source's selection of a sealed subtype applies
          val aSubtypeSelection = a.subtypeSelection()
          val bSubtypeSelection = b.subtypeSelection()
          val overridesSubtypeSelection = aSubtypeSelection != null && bSubtypeSelection != null
          val lower = if (overridesSubtypeSelection) b.withoutSubtypeSelection() else b
          val selectionOverrides = if (overridesSubtypeSelection) {
            listOf(OverridePath(a.path, aSubtypeSelection.pos, bSubtypeSelection.pos))
          } else emptyList()
          when (lower) {
            // if both are maps we merge
            is MapNode -> {
              val keys = a.map.keys + lower.map.keys
              val merges: Map<String, CascadeResult> =
                keys.associateWith { cascade(a.atKey(it), lower.atKey(it)) }
              // Also merge the maps' own scalar `value` (the "dual" leaf+map case) and fold in any
              // overrides it produces — otherwise a conflict on the value itself is silently dropped
              // and CascadeMode.Error would not flag it.
              val valueResult = cascade(a.value, lower.value)
              val overrides = merges.values.toList().flatMap { it.overrides } + valueResult.overrides + selectionOverrides
              val elements = merges.mapValues { it.value.node }
              CascadeResult(
                MapNode(elements, a.pos, a.path, valueResult.node, a.meta, a.delimiter, a.sourceKey),
                overrides
              )
            }
            // b is a scalar (or other non-map). Rather than discarding b entirely,
            // fold its value into a's `value` slot, with a's own value taking
            // precedence. Otherwise a higher-precedence source that turns this key
            // into a map (e.g. an env var HOST_X making `host` a map) would silently
            // drop a lower-precedence scalar at the same key, and decoding that key
            // as a scalar would then fail with "Missing" (gh-592).
            else -> {
              val value = cascade(a.value, lower)
              CascadeResult(
                MapNode(a.map, a.pos, a.path, value.node, a.meta, a.delimiter, a.sourceKey),
                value.overrides + selectionOverrides
              )
            }
          }
        }
      }
      // in explicit sealed types mode, a scalar can name the sealed subtype whose properties a lower priority map
      // sets, e.g. the env var `FOO=Bar` over a file's `foo { x = abc }`, so rather than discarding the map, fold
      // the scalar into it as its own value, replacing any selection of a subtype the map makes
      is StringNode if b is MapNode && sealedTypeDiscriminatorField != null -> {
        val bSubtypeSelection = b.subtypeSelection()
        val lower = if (bSubtypeSelection != null) b.withoutSubtypeSelection() as MapNode else b
        val overrides = if (bSubtypeSelection != null) {
          listOf(OverridePath(a.path, a.pos, bSubtypeSelection.pos))
        } else emptyList()
        CascadeResult(MapNode(lower.map, b.pos, b.path, a, b.meta, b.delimiter, b.sourceKey), overrides)
      }
      // since neither are maps, we just use this
      else -> CascadeResult(a, listOf(OverridePath(a.path, a.pos, b.pos)))
    }
  }

  /**
   * In explicit sealed types mode, a node selects a sealed subtype by its discriminator field, or by its own value
   * (as flat key sources such as env vars do, e.g. `FOO=Bar`). The subtype is selected by the highest priority source
   * that selects one, and the properties of all sources merge as usual, so that e.g. env vars can switch the subtype
   * that a config file selects, and the selected subtype determines which of the merged properties apply.
   *
   * Returns the node that selects a subtype: the discriminator field, this map's own value, or this string itself;
   * or null if this node doesn't select a subtype.
   */
  private fun Node.subtypeSelection(): StringNode? {
    val field = sealedTypeDiscriminatorField ?: return null
    return when (this) {
      is MapNode -> this[field] as? StringNode ?: value as? StringNode
      is StringNode -> this
      else -> null
    }
  }

  /**
   * Returns this node without its selection of a sealed subtype, see [subtypeSelection].
   */
  private fun Node.withoutSubtypeSelection(): Node = when (this) {
    is MapNode -> MapNode(map - sealedTypeDiscriminatorField!!, pos, path, Undefined, meta, delimiter, sourceKey)
    else -> Undefined
  }
}

enum class CascadeMode {
  Merge,
  Error, // throw an error if a config value has the same key as another

  @Deprecated("use Fallthrough")
  Override,
  Fallthrough,
}

data class CascadeResult(
  val node: Node,
  val overrides: List<OverridePath> = emptyList()
)

data class OverridePath(
  val path: DotPath,
  val overridePos: Pos,
  val overridenPos: Pos
)
