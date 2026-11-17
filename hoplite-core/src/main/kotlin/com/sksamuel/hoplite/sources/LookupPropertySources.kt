package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LookupPropertySource
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath

/**
 * A [LookupPropertySource] over a flat map of keys, such as environment variables or system properties.
 *
 * A path is read from one key, [key]. A key in the map matches it if the key, split on [delimiter], gives the same
 * canonical key. When several keys in the map match (they differ only in case or `-`, say), the one whose last
 * element is spelled exactly as the path's last element wins, so the map keys `a` and `A` stay apart, as do the
 * fields `someSetting` and `SOMESETTING`. Otherwise the canonical key itself wins, then the first in sorted order,
 * so a lookup gives the same answer on every machine.
 */
abstract class FlatMapLookupPropertySource(
  private val values: () -> Map<String, String>,
  private val pos: Pos,
) : LookupPropertySource {

  // the values, fixed on first use; see snapshot()
  private val map: Map<String, String> by lazy { values() }

  // canonical key -> the keys in the map that match it, the canonical key first, then in sorted order
  private val index: Map<String, List<String>> by lazy {
    map.keys.sorted()
      .groupBy { key(it.split(delimiter)) }
      .mapValues { (canonical, keys) -> keys.sortedBy { if (it == canonical) 0 else 1 } }
  }

  /**
   * Returns a tree-based source over [values], used by [node] to import the whole namespace.
   */
  protected abstract fun treeSource(values: () -> Map<String, String>): PropertySource

  override fun node(context: PropertySourceContext): ConfigResult<Node> = treeSource { map }.node(context)

  override fun lookup(path: List<String>): Node {
    if (path.isEmpty()) return Undefined
    // Case is ignored here, not below: key() gives the canonical spelling (for env vars, upper case without `-` or
    // `_`), and the index groups every key in the map under its canonical key. So the bucket holds all the keys that
    // differ only in case or `-`, such as `VALUES_a` and `VALUES_A`.
    val keys = index[key(path)] ?: return Undefined
    // The tie-break is case-sensitive on purpose: the key whose last element is spelled exactly as the path's wins,
    // so the map keys `a` and `A` each read their own variable. Otherwise the canonical key wins (it sorts first).
    val last = path.last()
    val key = keys.firstOrNull { it == last || it.endsWith(delimiter + last) } ?: keys.first()
    return StringNode(map.getValue(key), pos, DotPath(path), emptyMap(), delimiter, key)
  }

  override fun keysUnder(path: List<String>): Set<String> {
    val prefix = if (path.isEmpty()) "" else key(path) + delimiter
    // the next element starts after as many delimiters as the prefix has
    val depth = prefix.count { it == delimiter.single() }
    return index.entries
      .filter { it.key.startsWith(prefix) }
      .flatMap { it.value }
      .mapNotNull { it.split(delimiter).getOrNull(depth) }
      .filter { it.isNotEmpty() }
      .toSet()
  }
}

/**
 * A lookup-based source for environment variables, without a prefix (gh-606).
 *
 * Unlike [EnvironmentVariablesPropertySource], it doesn't import every variable into the config tree. The binder
 * looks up the variables a bound class needs, each from the one name [envName] gives: the field `maxPoolSize` in
 * `db` is read from `DB_MAXPOOLSIZE`, the name the tree-based source binds it from. Variables are matched ignoring
 * case and `-`, so `db_maxpoolsize` works too, but `DB_MAX_POOL_SIZE` is `db.max.pool.size`, as in Spring's relaxed
 * binding.
 *
 * Enable it for the default environment source with [com.sksamuel.hoplite.ConfigLoaderBuilder.withAmbientSourceLookup],
 * or add it directly.
 */
class EnvironmentVariablesLookupPropertySource(
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
) : FlatMapLookupPropertySource(environmentVariableMap, Pos.env) {

  override val delimiter: String = EnvironmentVariablesPropertySource.DELIMITER

  override fun source(): String = "Env Var (lookup)"

  override fun key(path: List<String>): String = envName(path)

  override fun treeSource(values: () -> Map<String, String>): PropertySource =
    EnvironmentVariablesPropertySource(values)

  override fun snapshot(): LookupPropertySource {
    val snapshot = environmentVariableMap()
    return EnvironmentVariablesLookupPropertySource { snapshot }
  }

  companion object {
    /**
     * The environment variable a config path is read from: each path element spelled as the
     * [com.sksamuel.hoplite.transformer.PathNormalizer] spells it (without `-` or `_`), the elements joined with `_`,
     * all in upper case. So `db.pool.maxPoolSize`, `db.pool.max-pool-size` and `db.pool.max_pool_size` are all read
     * from `DB_POOL_MAXPOOLSIZE`, and `DB_MAX_POOL_SIZE` is `db.max.pool.size`. An element that is a map key read
     * from the environment (such as `MY` and `KEY` of the key `MY_KEY`) has no `-` or `_` to remove, as the
     * variables are split on `_` into elements.
     */
    fun envName(path: List<String>): String =
      path.joinToString(EnvironmentVariablesPropertySource.DELIMITER) { it.replace("-", "").replace("_", "").uppercase() }
  }
}

/**
 * A lookup-based source for all system properties, as [SystemPropertiesPropertySource] with an empty prefix
 * provides (gh-606). The properties are snapshotted when the config is loaded.
 *
 * A path is read from the one name [propertyName] gives: path elements joined with `.`, matched ignoring case, `-`
 * and `_`, so the field `maxPoolSize` in `db` reads `db.maxPoolSize`, `db.max-pool-size` or `db.max_pool_size`.
 */
class SystemPropertiesLookupPropertySource(
  private val systemPropertiesMap: () -> Map<String, String> = { System.getProperties().toStringMap() },
) : FlatMapLookupPropertySource(systemPropertiesMap, Pos.sysprops) {

  override val delimiter: String = "."

  override fun source(): String = "System Properties (lookup)"

  override fun key(path: List<String>): String = propertyName(path)

  override fun treeSource(values: () -> Map<String, String>): PropertySource =
    SystemPropertiesPropertySource(values, prefix = "")

  override fun snapshot(): LookupPropertySource {
    val snapshot = systemPropertiesMap()
    return SystemPropertiesLookupPropertySource { snapshot }
  }

  companion object {
    /**
     * The system property a config path is read from, in canonical form: each path element spelled as the
     * [com.sksamuel.hoplite.transformer.PathNormalizer] spells it (lower case, without `-` or `_`), joined with `.`.
     */
    fun propertyName(path: List<String>): String =
      path.joinToString(".") { it.replace("-", "").replace("_", "").lowercase() }
  }
}

/**
 * Returns the lookup-based equivalent of this source if it reads an ambient namespace: environment variables
 * without a prefix, or system properties with an empty prefix. Otherwise returns this source.
 */
internal fun PropertySource.toAmbientLookupSource(): PropertySource = when {
  this is EnvironmentVariablesPropertySource && prefix == null ->
    EnvironmentVariablesLookupPropertySource(environmentVariableMap)
  // subclasses may change how the properties are read, so only the class itself (and its companion) is replaced
  this is SystemPropertiesPropertySource && prefix.isEmpty() &&
    (this::class == SystemPropertiesPropertySource::class || this === SystemPropertiesPropertySource) ->
    SystemPropertiesLookupPropertySource(systemPropertiesMap)
  else -> this
}
