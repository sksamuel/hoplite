package com.sksamuel.hoplite.hdfs

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import org.apache.hadoop.fs.Path
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class PathDecoder : NullHandlingDecoder<Path> {

  override fun supports(type: KType): Boolean = type.classifier == Path::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Path> {
    return when (node) {
      is StringNode -> Path(node.value).valid()
      else -> ConfigFailure.DecodeError(node, Path::class.createType()).invalid()
    }
  }
}
