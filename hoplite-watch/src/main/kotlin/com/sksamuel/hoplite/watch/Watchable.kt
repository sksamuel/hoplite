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
        // Without this guard, an exception thrown by callback() (or anything it calls
        // synchronously, like a misbehaving subscriber on the reload path) would propagate up to
        // the coroutine and silently terminate the watcher, with the user's errorHandler never
        // invoked. Funnel the failure through errorHandler and keep watching.
        try {
          callback()
        } catch (e: Throwable) {
          errorHandler(e)
        }
      }
    }
  }
}
