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
    // if I am null, and the other is defined, then that takes precedence
    is NullNode -> CascadeResult(other, listOf(this.path))
    is MapNode -> when {
      // in override mode, this entire map takes precendence.
      // note override only happens after root, otherwise one entire file would override another
      cascadeMode == CascadeMode.Override && this.path != DotPath.root -> CascadeResult(this)
      else -> {
        when (other) {
          // if both are maps we merge
          is MapNode -> {
            // overrides are any keys in the other that I also have.
            val overrides = other.map.filter { this.map.containsKey(it.key) }.map { it.value.path }
            val keys = this.map.keys + other.map.keys
            val map = keys.associateWith { this.atKey(it).cascade(other.atKey(it), cascadeMode).node }
            CascadeResult(
              MapNode(map, this.pos, this.path, this.value.cascade(other.value, cascadeMode).node),
              overrides
            )
          }
          else -> CascadeResult(this)
        }
      }
    }
    // if I am a non-null map then I take precedence
    else -> CascadeResult(this)
  }
}

enum class CascadeMode {
  Merge,
  Error,
  Warn,
  Override,
}

data class CascadeResult(
  val node: Node,
  val overrides: List<DotPath> = emptyList(),
)
