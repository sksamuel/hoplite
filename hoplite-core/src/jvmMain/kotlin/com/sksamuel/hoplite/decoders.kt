package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DefaultDecoderRegistry
import java.util.ServiceLoader

fun defaultDecoderRegistry(): DecoderRegistry {
  return defaultDecoderRegistry(Thread.currentThread().contextClassLoader)
}

fun defaultDecoderRegistry(classLoader: ClassLoader): DecoderRegistry {
  return ServiceLoader.load(Decoder::class.java, classLoader).toList().let { DefaultDecoderRegistry(it) }
}
