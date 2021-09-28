package com.sksamuel.hoplite.watch.watchers

import com.sksamuel.hoplite.watch.Watchable
import kotlinx.coroutines.asCoroutineDispatcher
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.Executors

class FileWatcher(private val dir: String) : Watchable {
  val watchService = FileSystems.getDefault().newWatchService()
  val pathToWatch = Paths.get(dir).toAbsolutePath()
  var cb = {}

  override fun watch(callback: () -> Unit) {
    cb = callback
  }

  init {
    pathToWatch.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE
    )

    Executors.newSingleThreadExecutor().asCoroutineDispatcher().executor.execute(Runnable {
      while (true) {
        val watchKey = watchService.take()
        if (watchKey.pollEvents().size > 0) {
          println("Reloading configuration")
          cb()
        }

        if (!watchKey.reset()) {
          watchKey.cancel()
          watchService.close()
          break
        }
      }
    })
  }
}
