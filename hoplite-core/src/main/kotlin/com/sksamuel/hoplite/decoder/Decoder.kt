package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

inline fun <reified T : Any> DecoderRegistry.decoder(): Decoder<T> = decoder(T::class)

interface DecoderRegistry {
  fun <T : Any> decoder(t: KClass<T>): Decoder<T>
  fun decoder(type: KType): ConfigResult<Decoder<*>>
  fun <T : Any> register(kclass: KClass<T>, factory: DecoderFactory)
}

fun defaultRegistry(): DecoderRegistry {
  return ServiceLoader
      .load(DecoderRegistration::class.java).toList()
      .fold(DefaultDecoderRegistry()) { registry, registration ->
        registration.register(registry)
        registry
      }
}

abstract class DefaultDecoderRegistry : DecoderRegistry {
}

interface Decoder<T> {
  fun convert(value: Value): ConfigResult<T>
}

interface DecoderFactory {
  fun build(type: KType, registry: DecoderRegistry): Decoder<*>?
}

interface DecoderRegistration {
  fun register(registry: DecoderRegistry)
}