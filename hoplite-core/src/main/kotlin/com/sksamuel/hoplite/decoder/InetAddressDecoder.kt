package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.net.InetAddress
import kotlin.reflect.KType

class InetAddressDecoder : NonNullableDecoder<InetAddress> {
  override fun supports(type: KType): Boolean = type.classifier == InetAddress::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<InetAddress> = when (node) {
    is StringNode -> Try { InetAddress.getByName(node.value) }.toValidated { ThrowableFailure(it) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

