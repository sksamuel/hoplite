package com.sksamuel.hoplite.watch

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.getOrElse
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class ReloadableConfig<A : Any>(private val configLoader: ConfigLoader, private val clazz: KClass<A>) {
  private val config = AtomicReference(configLoader.loadConfig(clazz, emptyList()).getOrElse { null })

  fun addWatcher(watchable: Watchable): ReloadableConfig<A> {
    watchable.watch { reloadConfig() }
    return this
  }

  private fun reloadConfig() {
    config.set(configLoader.loadConfig(clazz, emptyList()).getOrElse { null })
  }

  fun getLatest() : A? {
    return config.get()
  }
}

interface Watchable {
  fun watch(callback: () -> Unit)
}
