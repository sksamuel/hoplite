package com.sksamuel.hoplite.decoder

import arrow.core.Valid
import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

inline fun <reified T : Any> DecoderRegistry.decoder(): ConfigResult<Decoder<T>> = decoder(T::class)

/**
 * An immutable registry for providing instances of a [Decoder] for a given type.
 */
interface DecoderRegistry {

  fun <T : Any> decoder(t: KClass<T>): ConfigResult<Decoder<T>>
  fun decoder(type: KType): ConfigResult<Decoder<*>>
  fun register(decoder: Decoder<*>): DecoderRegistry

  companion object {
    val zero: DecoderRegistry = DefaultDecoderRegistry(emptyList())
  }
}

@Suppress("UNCHECKED_CAST")
class DefaultDecoderRegistry(private val decoders: List<Decoder<*>>) : DecoderRegistry {

  override fun <T : Any> decoder(t: KClass<T>): ConfigResult<Decoder<T>> {
    return decoder(t.createType()).map { it as Decoder<T> }
  }

  override fun decoder(type: KType): ConfigResult<Decoder<*>> =
    decoders.find { it.supports(type) }?.valid() ?: ConfigFailure.NoSuchDecoder(type).invalid()

  override fun register(decoder: Decoder<*>): DecoderRegistry = DefaultDecoderRegistry(decoders + decoder)
}

fun defaultDecoderRegistry(): DecoderRegistry {
  return ServiceLoader.load(Decoder::class.java).toList()
    .fold(DecoderRegistry.zero) { registry, decoder -> registry.register(decoder) }
}

/**
 * A typeclass for decoding a [Node] into a specified type.
 */
interface Decoder<T> {

  fun supports(type: KType): Boolean

  /**
   * Attempts to decode the given node into an instance of the given [KType].
   *
   * @param node contains the value for the current dot path
   * @param type the concrete type required by the caller
   * @param context used to lookup decoders for types that have nested types, and other context related things
   */
  fun decode(node: Node,
             type: KType,
             context: DecoderContext): ConfigResult<T>
}

inline fun <T, reified U> Decoder<T>.map(crossinline f: (T) -> U): Decoder<U> = object : Decoder<U> {
  override fun supports(type: KType): Boolean = U::class.createType() == type
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<U> {
    return this@map.decode(node, type, context).map { f(it) }
  }
}

/**
 * Extends [Decoder] for types which will not handle nulls.
 */
@Suppress("UNCHECKED_CAST")
interface NonNullableDecoder<T> : Decoder<T> {

  // if we have a nullable value and the type is nullable that is ok
  private fun offerNull(node: Node, type: KType): Validated<ConfigFailure, *> {
    return if (type.isMarkedNullable) Valid(null) else
      ConfigFailure.NullValueForNonNullField(node).invalid()
  }

  // if we have no value defined and the type is nullable, that is ok
  private fun offerUndefined(type: KType): Validated<ConfigFailure, *> {
    return if (type.isMarkedNullable) Valid(null) else
      ConfigFailure.MissingValue.invalid()
  }

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): Validated<ConfigFailure, T> =
    when (node) {
      is Undefined -> offerUndefined(type).map { it as T }
      is NullValue -> offerNull(node, type).map { it as T }
      else -> safeDecode(node, type, context)
    }

  /**
   * Attempts to decode the given node into an instance of the given [KType].
   *
   * @param node contains the value for the current dot path
   * @param type the concrete type required by the caller
   * @param context used to lookup decoders for types that have nested types
   */
  fun safeDecode(node: Node,
                 type: KType,
                 context: DecoderContext): ConfigResult<T>
}

interface NonNullableLeafDecoder<T> : NonNullableDecoder<T> {

  fun safeLeafDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T>

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> {
    return when (node) {
      is MapNode -> decode(node.value, type, context)
      else -> safeLeafDecode(node, type, context)
    }
  }
}
