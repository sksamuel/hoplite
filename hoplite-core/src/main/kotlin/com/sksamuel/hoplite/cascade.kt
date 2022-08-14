@file:JvmName("CascadeKt")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath

/**
 * Returns a new [Node] which is the result of merging this node with keys from the [other] node,
 * with keys in this node taking preccence if they are defined and not null.
 *
 * @return a [CascadeResult] containing the resolved [Node], and a list of overrides.
 */
internal fun Node.cascade(other: Node, cascadeMode: CascadeMode): CascadeResult {

  if (other is Undefined) return CascadeResult(this)
  if (this is Undefined) return CascadeResult(other)

  require(this.path == other.path) {
    "Trying to merge node with path ${path.flatten()} with node with path ${other.path.flatten()}"
  }

  return when (this) {
    // if I am null, then we should use the other one, but that is not an override
    is NullNode -> CascadeResult(other)
    is MapNode -> when {
      // in override mode, this entire map takes precendence.
      // note override only happens after root, otherwise one entire file would override another
      cascadeMode == CascadeMode.Override && this.path != DotPath.root -> CascadeResult(this)
      else -> {
        when (other) {
          // if both are maps we merge
          is MapNode -> {
            val keys = this.map.keys + other.map.keys
            val merges: Map<String, CascadeResult> = keys.associateWith { this.atKey(it).cascade(other.atKey(it), cascadeMode) }
            val overrides = merges.values.toList().flatMap { it.overrides }
            val elements = merges.mapValues { it.value.node }
            CascadeResult(
              MapNode(elements, this.pos, this.path, this.value.cascade(other.value, cascadeMode).node),
              overrides
            )
          }
          // since the other once is not a map, we override completely with this
          else -> CascadeResult(this, listOf(OverridePath(this.path, this.pos, other.pos)))
        }
      }
    }
    // since neither are maps, we just use this
    else -> CascadeResult(this, listOf(OverridePath(this.path, this.pos, other.pos)))
  }
}

enum class CascadeMode {
  Merge,
  Error,
  Override,
}

data class CascadeResult(
  val node: Node,
  val overrides: List<OverridePath> = emptyList(),
)

data class OverridePath(
  val path: DotPath,
  val overridePos: Pos,
  val overridenPos: Pos,
)
