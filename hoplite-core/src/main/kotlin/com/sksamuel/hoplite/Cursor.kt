package com.sksamuel.hoplite

interface Cursor {

  /**
   * The file system location of the config to which this cursor points.
   */
  fun location(): ConfigLocation? = null

  /**
   * Returns a cursor pointing to the [Value] stored at the given key of this cursor.
   */
  fun atKey(key: String): Cursor

  fun atPath(path: String): Cursor {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  /**
   * Returns the [Value] that this cursor points to.
   */
  fun value(): Value

  /**
   * The path in the config to which this cursor points as a list of keys, with the
   * first key in the result being the outermost part of the path.
   */
  fun path(): List<String> = emptyList()
}