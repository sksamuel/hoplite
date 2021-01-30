package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode

fun defaultPreprocessors() = listOf(
  EnvVarPreprocessor,
  SystemPropertyPreprocessor,
  RandomPreprocessor,
  LookupPreprocessor
)

