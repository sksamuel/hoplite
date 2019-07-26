package com.sksamuel.hoplite.decoder

import arrow.data.validNel
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigResults
import com.sksamuel.hoplite.Node
import java.lang.reflect.ParameterizedType
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
  override fun <T : Any> decoder(t: KClass<T>): ConfigResult<Decoder<T>> = decoder(t.createType()).map { it as Decoder<T> }
  override fun decoder(type: KType): ConfigResult<Decoder<*>> =
      decoders.find { it.supports(type) }?.validNel() ?: ConfigResults.NoSuchDecoder(type)
  override fun register(decoder: Decoder<*>): DecoderRegistry = DefaultDecoderRegistry(decoders + decoder)
}

fun defaultRegistry(): DecoderRegistry {
  return ServiceLoader.load(Decoder::class.java).toList()
      .fold(DecoderRegistry.zero) { registry, decoder -> registry.register(decoder) }
}

/**
 * A typeclass for decoding a [Node] into a specified type.
 */
interface Decoder<T> {
  fun supports(type: KType): Boolean
  fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<T>
}

interface BasicDecoder<T> : Decoder<T> {
  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<T> = decode(node)
  fun decode(node: Node): ConfigResult<T>
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