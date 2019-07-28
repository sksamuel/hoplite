package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import java.security.Principal
import kotlin.reflect.KType

class PrincipalDecoder : BasicDecoder<Principal> {
  override fun supports(type: KType): Boolean = type.classifier == Principal::class
  override fun decode(node: Node, path: String): ConfigResult<Principal> = when (node) {
    is StringNode -> BasicPrincipal(node.value).validNel()
    else -> ConfigFailure.conversionFailure<Principal>(node).invalidNel()
  }
}

data class BasicPrincipal(private val name: String) : Principal {
  override fun getName(): String = name
}
