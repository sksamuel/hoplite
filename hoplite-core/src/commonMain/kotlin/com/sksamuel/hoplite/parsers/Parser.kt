package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.Node

interface Parser {
  fun load(bytes: ByteArray, source: String): Node
  fun defaultFileExtensions(): List<String>
}
