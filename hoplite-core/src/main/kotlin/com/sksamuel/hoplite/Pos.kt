package com.sksamuel.hoplite

/**
 * Tracks where in a file a config value was retrieved.
 */
sealed class Pos {

  /**
   * Used if no positional information was present. For example, an environment
   * variable would not have file information.
   */
  object NoPos : Pos()

  /**
   * Used when the only information available is the file name.
   * For example, when processing from an input stream.
   */
  data class FilePos(val source: String) : Pos()

  /**
   * Used when we know the filename and the line.
   */
  data class LinePos(val line: Int, val source: String) : Pos()

  /**
   * Used when we know the filename, line and columnn.
   */
  data class LineColPos(val line: Int, val col: Int, val source: String) : Pos()

  fun source(): String? = when (this) {
    is FilePos -> this.source
    is LineColPos -> this.source
    is LinePos -> this.source
    NoPos -> null
  }
}

fun Pos.loc() = when (this) {
  is Pos.NoPos -> ""
  is Pos.FilePos -> "($source)"
  is Pos.LineColPos -> "($source:$line:$col)"
  is Pos.LinePos -> "($source:$line)"
}
