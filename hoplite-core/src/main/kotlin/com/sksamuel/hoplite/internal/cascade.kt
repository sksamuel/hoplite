package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

class Cascader(
  private val cascadeMode: CascadeMode,
  private val allowEmptyTree: Boolean,
  private val allowNullOverride: Boolean = false,
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
   * with keys in node a taking preccence if they are defined and not null.
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
          when (b) {
            // if both are maps we merge
            is MapNode -> {
              val keys = a.map.keys + b.map.keys
              val merges: Map<String, CascadeResult> =
                keys.associateWith { cascade(a.atKey(it), b.atKey(it)) }
              val overrides = merges.values.toList().flatMap { it.overrides }
              val elements = merges.mapValues { it.value.node }
              CascadeResult(
                MapNode(elements, a.pos, a.path, cascade(a.value, b.value).node),
                overrides
              )
            }
            // since the other once is not a map, we override completely with this
            else -> CascadeResult(a, listOf(OverridePath(a.path, a.pos, b.pos)))
          }
        }
      }
      // since neither are maps, we just use this
      else -> CascadeResult(a, listOf(OverridePath(a.path, a.pos, b.pos)))
    }
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
