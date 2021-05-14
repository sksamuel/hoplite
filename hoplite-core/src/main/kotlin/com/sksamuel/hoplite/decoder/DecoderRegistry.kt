package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * An immutable registry for providing instances of a [Decoder] for a given type.
 */
interface DecoderRegistry {

  /**
   * Returns a [Decoder] for the given kclass otherwise returns an invalid ConfigFailure.
   */
  fun <T : Any> decoder(t: KClass<T>): ConfigResult<Decoder<T>>

  fun decoder(type: KType): ConfigResult<Decoder<*>>

  fun register(decoder: Decoder<*>): DecoderRegistry

  val size: Int

  companion object {
    val zero: DecoderRegistry = DefaultDecoderRegistry(emptyList())
  }
}

inline fun <reified T : Any> DecoderRegistry.decoder(): ConfigResult<Decoder<T>> = decoder(T::class)

@Suppress("UNCHECKED_CAST")
class DefaultDecoderRegistry(private val decoders: List<Decoder<*>>) : DecoderRegistry {

  override fun <T : Any> decoder(t: KClass<T>): ConfigResult<Decoder<T>> {
    return decoder(t.createType()).map { it as Decoder<T> }
  }

  override fun decoder(type: KType): ConfigResult<Decoder<*>> {
    require(decoders.isNotEmpty()) { "Cannot find decoder in empty decoder registry" }
    require(type.classifier is KClass<*>) { "Only instances of KClass are supported [was ${type.classifier}]" }
    val filteredDecoders = decoders.filter { it.supports(type) }
    return when {
      filteredDecoders.isEmpty() && (type.classifier as KClass<*>).isData -> ConfigFailure.NoDataClassDecoder.invalid()
      filteredDecoders.isEmpty() -> ConfigFailure.NoSuchDecoder(type, decoders).invalid()
      else -> filteredDecoders.maxByOrNull { it.priority() }!!.valid()
    }
  }

  override fun register(decoder: Decoder<*>): DecoderRegistry = DefaultDecoderRegistry(decoders + decoder)

  override val size: Int = decoders.size
}

fun defaultDecoderRegistry(): DecoderRegistry {
  return defaultDecoderRegistry(Thread.currentThread().contextClassLoader)
}

fun defaultDecoderRegistry(classLoader: ClassLoader): DecoderRegistry {
  return ServiceLoader.load(Decoder::class.java, classLoader).toList().let { DefaultDecoderRegistry(it) }
}
