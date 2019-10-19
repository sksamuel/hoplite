package com.sksamuel.hoplite.cronutils

import arrow.core.invalid
import arrow.core.valid
import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NonNullableDecoder
import kotlin.reflect.KType

class CronDecoder : NonNullableDecoder<Cron> {

  override fun supports(type: KType): Boolean = type.classifier == Cron::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Cron> {
    fun fromUnixExpression(expr: String): ConfigResult<Cron> = kotlin.runCatching {
      val def = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
      CronParser(def).parse(expr).valid()
    }.getOrElse { ConfigFailure.Generic("Could not create cron from expression [$expr]").invalid() }

    return when (node) {
      is StringNode -> fromUnixExpression(node.value)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
