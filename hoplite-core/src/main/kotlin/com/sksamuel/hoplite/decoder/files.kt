package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KType

class FileDecoder : NullHandlingDecoder<File> {
  override fun supports(type: KType): Boolean = type.classifier == File::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<File> = when (node) {
    is StringNode -> File(node.value).valid()
    is MapNode -> safeDecode(node.value, type, context)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PathDecoder : NullHandlingDecoder<Path> {
  override fun supports(type: KType): Boolean = type.classifier == Path::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Path> = when (node) {
    is StringNode -> Paths.get(node.value).valid()
    is MapNode -> safeDecode(node.value, type, context)
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
