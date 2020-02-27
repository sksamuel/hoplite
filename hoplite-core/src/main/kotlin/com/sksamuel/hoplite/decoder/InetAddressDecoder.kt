package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.fp.Try
import java.net.InetAddress
import kotlin.reflect.KType

class InetAddressDecoder : NullHandlingDecoder<InetAddress> {
  override fun supports(type: KType): Boolean = type.classifier == InetAddress::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<InetAddress> = when (node) {
    is StringNode -> Try { InetAddress.getByName(node.value) }.toValidated { ThrowableFailure(it) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

