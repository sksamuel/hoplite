package com.sksamuel.hoplite.watch

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.getOrElse
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class ReloadableConfig<A : Any>(private val configLoader: ConfigLoader, private val clazz: KClass<A>) {
  private val config = AtomicReference(configLoader.loadConfig(clazz, emptyList()).getOrElse { null })
  private var errorHandler: ((Throwable) -> Unit)? = null

  fun addWatcher(watchable: Watchable): ReloadableConfig<A> {
    watchable.watch(
      { reloadConfig() },
      { errorHandler?.invoke(it) }
    )
    return this
  }

  fun addErrorHandler(handler: (Throwable) -> Unit): ReloadableConfig<A> {
    errorHandler = handler
    return this
  }

  private fun reloadConfig() {
    kotlin.runCatching { configLoader.loadConfigOrThrow(clazz, emptyList()) }
      .fold(
        { config.set(it) },
        { errorHandler?.invoke(it) }
      )
  }

  fun getLatest() : A? {
    return config.get()
  }
}

interface Watchable {
  fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit)
}
