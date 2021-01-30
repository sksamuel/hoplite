package com.sksamuel.hoplite.decoder

import kotlin.reflect.KType

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

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): Validated<ConfigFailure, T> =
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
  fun safeDecode(node: Node,
                 type: KType,
                 context: DecoderContext): ConfigResult<T>
}
