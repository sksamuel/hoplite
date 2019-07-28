package com.sksamuel.hoplite.hdfs

import arrow.data.invalidNel
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.BasicDecoder
import org.apache.hadoop.fs.Path
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class PathDecoder : BasicDecoder<Path> {

  override fun supports(type: KType): Boolean = type.classifier == Path::class

  override fun decode(node: Node, path: String): ConfigResult<Path> {
    return when (node) {
      is StringNode -> Path(node.value).valid()
      else -> ConfigFailure.TypeConversionFailure(node, path, Path::class.createType()).invalidNel()
    }
  }
}
