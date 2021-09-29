package com.sksamuel.hoplite.watcher.consul

import com.orbitz.consul.KeyValueClient
import com.orbitz.consul.cache.KVCache
import com.sksamuel.hoplite.watch.Watchable

class ConsulWatcher(private val kvClient: KeyValueClient, private val paths: List<String>) : Watchable {
  companion object {
    const val WatchSeconds = 3
  }
  override fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    paths.forEach {
      val cache = KVCache.newCache(kvClient, it, WatchSeconds)
      cache.addListener { callback() }
      cache.start()
    }
  }
}
