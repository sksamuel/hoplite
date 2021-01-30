package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import kotlin.reflect.KType

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
   * By default all Hoplite decoders have the minimum priority so user's can override and allow their
   * decoders to take priority.
   */
  fun priority(): Int = Int.MIN_VALUE

  /**
   * Attempts to decode the given node into an instance of the given [KType].
   *
   * @param node contains the value for the current dot path
   * @param type the concrete type required by the caller
   * @param context used to lookup decoders for types that have nested types, and other context related things
   */
  fun decode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<T>
}
