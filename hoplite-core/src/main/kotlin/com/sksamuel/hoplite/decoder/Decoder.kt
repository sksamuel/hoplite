package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

inline fun <reified T : Any> DecoderRegistry.decoder(path: String): ConfigResult<Decoder<T>> = decoder(T::class, path)

/**
 * An immutable registry for providing instances of a [Decoder] for a given type.
 */
interface DecoderRegistry {

  fun <T : Any> decoder(t: KClass<T>, path: String): ConfigResult<Decoder<T>>
  fun decoder(type: KType, path: String): ConfigResult<Decoder<*>>
  fun register(decoder: Decoder<*>): DecoderRegistry

  companion object {
    val zero: DecoderRegistry = DefaultDecoderRegistry(emptyList())
  }
}

@Suppress("UNCHECKED_CAST")
class DefaultDecoderRegistry(private val decoders: List<Decoder<*>>) : DecoderRegistry {

  override fun <T : Any> decoder(t: KClass<T>, path: String): ConfigResult<Decoder<T>> =
    decoder(t.createType(), path).map { it as Decoder<T> }

  override fun decoder(type: KType, path: String): ConfigResult<Decoder<*>> =
    decoders.find { it.supports(type) }?.validNel() ?: ConfigFailure.NoSuchDecoder(type, path).invalidNel()

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
   * @param registry used to lookup decoders for types that have nested types
   * @param path dot seperated path to the field currently being decoded.
   */
  fun decode(node: Node,
             type: KType,
             registry: DecoderRegistry,
             path: String): ConfigResult<T>
}

interface BasicDecoder<T> : Decoder<T> {
  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<T> = decode(node, path)

  fun decode(node: Node, path: String): ConfigResult<T>
}

val Any.typeParameters: List<Class<*>?>
  get() {
    val ptype = this.javaClass.genericSuperclass as ParameterizedType
    return ptype.actualTypeArguments.map {
      when (it) {
        is Class<*> -> it
        else -> null
      }
    }
  }
