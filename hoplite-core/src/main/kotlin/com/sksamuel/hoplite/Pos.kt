package com.sksamuel.hoplite

/**
 * Tracks where in a file a config value was retrieved.
 */
sealed class Pos {

  /**
   * Used if no positional information was present.
   */
  object NoPos : Pos()

  /**
   * Used when the only information available is a source name, such as env-vars or input streams.
   */
  data class SourcePos(val source: String) : Pos()

  /**
   * Used when we know the filename and the line.
   */
  data class LinePos(val line: Int, val source: String) : Pos()

  /**
   * Used when we know the filename, line and columnn.
   */
  data class LineColPos(val line: Int, val col: Int, val source: String) : Pos()

  fun source(): String? = when (this) {
    is SourcePos -> this.source
    is LineColPos -> this.source
    is LinePos -> this.source
    NoPos -> null
  }

  companion object {
    val env = SourcePos("env")
  }
}

fun Pos.loc() = when (this) {
  is Pos.NoPos -> ""
  is Pos.SourcePos -> "($source)"
  is Pos.LineColPos -> "($source:$line:$col)"
  is Pos.LinePos -> "($source:$line)"
}
