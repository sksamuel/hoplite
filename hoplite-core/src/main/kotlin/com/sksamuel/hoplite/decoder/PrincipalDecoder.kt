package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import java.security.Principal
import kotlin.reflect.KType

class PrincipalDecoder : NonNullableLeafDecoder<Principal> {
  override fun supports(type: KType): Boolean = type.classifier == Principal::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Principal> = when (node) {
    is StringNode -> BasicPrincipal(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

data class BasicPrincipal(private val name: String) : Principal {
  override fun getName(): String = name
}
