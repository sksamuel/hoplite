package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

object SystemContextResolver : ContextResolver() {

  override val contextKey: String = "system"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return when (path) {
      "processors" -> Runtime.getRuntime().availableProcessors().toString().valid()
      "timestamp" -> System.currentTimeMillis().toString().valid()
      else -> ConfigFailure.ResolverFailure("Uknown system context path $path").invalid()
    }
  }
}
