package com.sksamuel.hoplite.watch

import kotlinx.coroutines.delay

interface Watchable {
  suspend fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit)
}

class FixedIntervalWatchable(private val intervalMs: Long) : Watchable {
  override suspend fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    while (true) {
      delay(intervalMs)
      callback()
    }
  }
}
