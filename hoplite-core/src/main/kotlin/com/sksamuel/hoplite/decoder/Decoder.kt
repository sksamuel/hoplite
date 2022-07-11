package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.invalid
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * A typeclass for decoding a [Node] into a specified type.
 */
interface Decoder<T> {

  /**
   * Returns true if this decoder can decode values of the given type.
   * This method is called by the framework to locate a decoder suitable for a particular type.
   */
  fun supports(type: KType): Boolean

  /**
   * If multiple decoders can support the same type, then the one with the highest priority value will win.
   * In the case of ties, the decoder will be picked arbitrarily.
   *
   * By default, all Hoplite decoders have the minimum priority so user decoders always take precedence.
   */
  fun priority(): Int = if (this.javaClass.`package`.name.startsWith("com.sksamuel.hoplite")) -100 else 0

  /**
   * Attempts to decode the given node into an instance of the given [KType].
   *
   * @param node the node for the current path
   * @param type the concrete type required by the caller, used by decoders that support type hierarchies
   * @param context used to lookup decoders for types that have nested types.
   */
  fun decode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<T>
}

data class DotPath(val keys: List<String>) {
  constructor(vararg keys: String) : this(keys.toList())

  companion object {
    val root = DotPath(emptyList())
  }

  fun with(name: String): DotPath = DotPath(keys + name)
  fun flatten() = keys.joinToString(".")
}

inline fun <T, reified U> Decoder<T>.map(crossinline f: (T) -> U): Decoder<U> = object : Decoder<U> {
  override fun supports(type: KType): Boolean = U::class.createType() == type
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<U> {
    return this@map.decode(node, type, context).map { f(it) }
  }
}

/**
 * Extends [Decoder] to provide support for nullable types. Other decoders can extend this
 * interface and not worry about dealing with nullability concerns, as this implementation
 * will check for null before calling down.
 */
@Suppress("UNCHECKED_CAST")
interface NullHandlingDecoder<T> : Decoder<T> {

  /**
   * If the requested type is nullable returns null as a value, otherwise
   * returns an error indicating that a nullable was provided to a non-null field.
   */
  private fun offerNull(node: Node, type: KType): Validated<ConfigFailure, *> {
    return if (type.isMarkedNullable) Validated.Valid(null) else
      ConfigFailure.NullValueForNonNullField(node).invalid()
  }

  /**
   * If the requested type is nullable returns null as a value, otherwise
   * returns an error indicating that a value was missing for a non-null field.
   */
  private fun offerUndefined(type: KType): Validated<ConfigFailure, *> {
    return if (type.isMarkedNullable) Validated.Valid(null) else
      ConfigFailure.MissingValue.invalid()
  }

  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> =
    when (node) {
      is Undefined -> offerUndefined(type).map { it as T }
      is NullNode -> offerNull(node, type).map { it as T }
      else -> safeDecode(node, type, context)
    }

  /**
   * Attempts to decode the given node into a non-null instance of the given [KType].
   *
   * @param node contains the value for the current dot path
   * @param type the concrete type required by the caller
   * @param context used to lookup decoders for types that have nested types
   */
  fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<T>
}

interface NonNullableLeafDecoder<T> : NullHandlingDecoder<T> {

  fun safeLeafDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T>

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> {
    return when (node) {
      is MapNode -> decode(node.value, type, context)
      else -> safeLeafDecode(node, type, context)
    }
  }
}
