package com.sksamuel.hoplite.watch

import com.sksamuel.hoplite.ConfigLoader
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

/**
 * A [ReloadableConfig] accepts a [ConfigLoader] and a target config [KClass].
 *
 * One or more [Watchable]s can be added to this class, and when they trigger an update,
 * the config is reloaded.
 *
 * You can retrieve the latest config at any time with [getLatest].
 */
class ReloadableConfig<A : Any>(
  private val configLoader: ConfigLoader,
  private val clazz: KClass<A>
) {

  private val config = AtomicReference(configLoader.loadConfigOrThrow(clazz, emptyList()))
  private var errorHandler: ((Throwable) -> Unit)? = null

  fun addWatcher(watchable: Watchable): ReloadableConfig<A> {
    watchable.watch(
      { reloadConfig() },
      { errorHandler?.invoke(it) }
    )
    return this
  }

  /**
   * Add a watcher that refresh this config on a fixed interval.
   */
  fun addInterval(millis: Long): ReloadableConfig<A> {
    FixedIntervalWatchable(millis).watch(
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

  fun getLatest(): A {
    return config.get()
  }
}
