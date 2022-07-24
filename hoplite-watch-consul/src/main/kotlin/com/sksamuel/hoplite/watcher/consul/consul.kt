package com.sksamuel.hoplite.watcher.consul

import com.orbitz.consul.KeyValueClient
import com.orbitz.consul.cache.KVCache
import com.sksamuel.hoplite.watch.Watchable

class ConsulWatcher(
  private val kvClient: KeyValueClient, private val paths: List<String>,
  private val watchSeconds: Int = WatchSeconds,
) : Watchable {

  companion object {
    const val WatchSeconds = 5
  }

  override suspend fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    paths.forEach {
      val cache = KVCache.newCache(kvClient, it, watchSeconds)
      cache.addListener { callback() }
      cache.start()
    }
  }

}
