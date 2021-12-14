package com.sksamuel.hoplite

/**
 * Tracks where in a file a config value was retrieved.
 */
sealed class Pos {

  /**
   * Used if no positional information was present. For example, an environment
   * variable would not have file information.
   */
  object None : Pos()

  /**
   * Used when the only information available is the name of the source, such as the filename,
   * or a generic name, such as 'env-vars'.
   */
  data class SourceNamePos(val source: String) : Pos()

  /**
   * Used when we know the filename and the line.
   */
  data class LinePos(val line: Int, val source: String) : Pos()

  /**
   * Used when we know the filename, line and columnn.
   */
  data class LineColPos(val line: Int, val col: Int, val source: String) : Pos()
}

fun Pos.loc() = when (this) {
  is Pos.None -> ""
  is Pos.SourceNamePos -> "($source)"
  is Pos.LineColPos -> "($source:$line:$col)"
  is Pos.LinePos -> "($source:$line)"
}
