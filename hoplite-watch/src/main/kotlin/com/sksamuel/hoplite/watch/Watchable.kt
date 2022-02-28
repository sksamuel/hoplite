package com.sksamuel.hoplite.watch

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface Watchable {
  fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit)
}

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
