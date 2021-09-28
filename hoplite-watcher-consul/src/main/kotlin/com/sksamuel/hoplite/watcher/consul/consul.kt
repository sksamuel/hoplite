package com.sksamuel.hoplite.watch.watchers

import com.orbitz.consul.cache.KVCache
import com.sksamuel.hoplite.watch.Watchable

class ConsulWatcher(private val kvCache: KVCache) : Watchable {
  override fun watch(callback: () -> Unit) {
    kvCache.addListener {
      callback()
    }
    kvCache.start()
  }
}
