package com.sksamuel.hoplite.watch

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface Watchable {
  fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit)
}

@OptIn(DelicateCoroutinesApi::class)
class FixedIntervalWatchable(private val intervalMs: Long) : Watchable {
  override fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    GlobalScope.launch {
      while (isActive) {
        delay(intervalMs)
        callback()
      }
    }
  }
}
