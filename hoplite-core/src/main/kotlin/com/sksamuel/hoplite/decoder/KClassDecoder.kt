package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import kotlin.reflect.KClass
import kotlin.reflect.KType

class KClassDecoder : NullHandlingDecoder<KClass<*>> {
  override fun supports(type: KType): Boolean = type.classifier == KClass::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<KClass<*>> = when (node) {
    is StringNode -> kotlin.runCatching { Class.forName(node.value).kotlin }.toValidated { ThrowableFailure(it) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

