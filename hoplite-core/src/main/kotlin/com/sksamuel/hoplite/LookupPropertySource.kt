package com.sksamuel.hoplite

/**
 * A [PropertySource] over an ambient namespace, such as unprefixed environment variables, where most keys are not
 * config for this application. Rather than importing the whole namespace into the config tree, the binder asks a
 * lookup source for the keys the bound class needs, at decode time (gh-606).
 *
 * Paths are given as lists of elements: the names of data class fields (as the
 * [com.sksamuel.hoplite.ParameterMapper]s give them), map keys and list indices. The source maps each path to one
 * key of its own, [key]: for example, the environment variable source reads `db.pool.maxPoolSize` from
 * `DB_POOL_MAXPOOLSIZE`.
 *
 * [node] imports the whole namespace into a tree, as a tree-based source does. It is used where there is no target
 * type to look up keys for, such as [ConfigLoader.loadNode].
 */
interface LookupPropertySource : PropertySource {

  /**
   * The delimiter this source puts between path elements in its own keys, used to join several path elements
   * into one map key. For example `_` for environment variables.
   */
  val delimiter: String

  /**
   * Returns the one key in this source that [path] is read from, in canonical form. Every key the source has is
   * matched against it after the same normalization (see the implementations), so the canonical form decides what
   * a key may differ in, such as case. Paths with the same key are the same path to this source.
   */
  fun key(path: List<String>): String

  /**
   * Returns the value of the key [key] gives for [path], or [Undefined] if this source has no value there. The returned node should
   * carry the source's own key as its [Node.sourceKey], so the report can show where the value came from.
   */
  fun lookup(path: List<String>): Node

  /**
   * Returns the path elements that follow [path] in this source's keys, spelled as in the source: a prefix scan,
   * used where the binder needs to know which keys exist, such as for maps, lists, sealed types and decoders it
   * can't see into. For example, given the environment variables `LABELS_ENV` and `LABELS_MY_KEY`, the keys
   * under `labels` are `ENV` and `MY`.
   */
  fun keysUnder(path: List<String>): Set<String>

  /**
   * Called once when the config is loaded. Returns a source that answers every later [lookup] and [keysUnder]
   * from the values at that moment, so all binds from one load see the same values.
   */
  fun snapshot(): LookupPropertySource = this
}
