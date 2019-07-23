package com.sksamuel.hoplite

import java.io.InputStream

interface Parser {
  fun load(input: InputStream): Value
}