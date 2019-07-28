package com.sksamuel.hoplite

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

typealias ConfigResult<A> = ValidatedNel<ConfigFailure, A>

object ConfigResults {
  fun NoSuchParser(ext: String): ConfigResult<Nothing> = ConfigFailure("No such parser for ext $ext").invalidNel()

  fun NoSuchDecoder(type: KType): ConfigResult<Nothing> = ConfigFailure("No such decoder for $type").invalidNel()

  fun decodeFailure(node: Node, path: String, target: KClass<*>): ConfigResult<Nothing> =
    ConfigFailure.TypeConversionFailure(node, path, target.createType()).invalidNel()

  fun decodeFailure(node: Node, target: Class<*>?): ConfigResult<Nothing> = TODO()
  fun decodeFailure(node: Node, error: String): ConfigResult<Nothing> = TODO()
  fun failed(description: String): ConfigResult<Nothing> = ConfigFailure(description).invalidNel()
  fun failedTypeConversion(node: Node): ConfigResult<Nothing> = ConfigFailure("type conversion failure at $node").invalidNel()
}
