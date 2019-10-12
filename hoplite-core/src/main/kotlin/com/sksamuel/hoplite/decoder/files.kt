package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.StringNode
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KType

class FileDecoder : NonNullableDecoder<File> {
  override fun supports(type: KType): Boolean = type.classifier == File::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<File> = when (node) {
    is StringNode -> File(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PathDecoder : NonNullableDecoder<Path> {
  override fun supports(type: KType): Boolean = type.classifier == Path::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Path> = when (node) {
    is StringNode -> Paths.get(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
