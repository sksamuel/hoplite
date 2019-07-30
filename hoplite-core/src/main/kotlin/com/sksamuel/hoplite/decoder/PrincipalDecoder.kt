package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import java.security.Principal
import kotlin.reflect.KType

class PrincipalDecoder : NonNullableDecoder<Principal> {
  override fun supports(type: KType): Boolean = type.classifier == Principal::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Principal> = when (node) {
    is StringNode -> BasicPrincipal(node.value).valid()
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}

data class BasicPrincipal(private val name: String) : Principal {
  override fun getName(): String = name
}
