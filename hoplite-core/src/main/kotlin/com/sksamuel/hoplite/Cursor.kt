package com.sksamuel.hoplite

/**
 * A wrapper for a `ConfigValue` providing safe navigation through the config and holding positional data for better
 * error handling.
 */
interface Cursor {

  fun value(): Any?

  /**
   * The path in the config to which this cursor points as a list of keys in reverse order (deepest key first).
   */
  fun pathElems(): List<String> = emptyList()

  /**
   * The path in the config to which this cursor points.
   */
  fun path(): String = pathElems().reversed().joinToString(".")

  /**
   * The file system location of the config to which this cursor points.
   */
  fun location(): ConfigLocation? = null

  /**
   * Returns whether this cursor points to an undefined value. A cursor can point to an undefined value when a missing
   * config key is requested or when a `null` `ConfigValue` is provided, among other reasons.
   *
   * @return `true` if this cursor points to an undefined value, `false` otherwise.
   */
  fun isUndefined(): Boolean = false

  /**
   * Returns whether this cursor points to a `null` config value. An explicit `null` value is different than a missing
   * value - `isUndefined` can be used to check for the latter.
   *
   * @return `true` if this cursor points to a `null` value, `false` otherwise.
   */
  fun isNull(): Boolean = false

  fun atPath(path: String): ConfigResult<Cursor> = ConfigResults.failed("Cannot nest path for primitive type")// = fluent.at(pathSegments: _*).cursor
}