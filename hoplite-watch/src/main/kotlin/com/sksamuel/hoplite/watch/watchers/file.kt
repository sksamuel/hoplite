package com.sksamuel.hoplite.watch.watchers

import com.sksamuel.hoplite.watch.Watchable
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.Executors

class FileWatcher(private val dir: String) : Watchable {
  private var cb = {}
  private var errorCallback: ((Throwable) -> Unit) = { }

  override fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    cb = callback
    errorCallback = errorHandler
    start()
  }

  private fun start() {
    val watchService = FileSystems.getDefault().newWatchService()
    val pathToWatch = Paths.get(dir).toAbsolutePath()

    pathToWatch.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE
    )

    Executors.newSingleThreadExecutor().submit {
      while (true) {
        try {
          val watchKey = watchService.take()
          if (watchKey != null) {
            val events = watchKey.pollEvents()
            if (events.size > 0) {
              cb()
            }

            if (!watchKey.reset()) {
              watchKey.cancel()
              watchService.close()
              break
            }
          }
        } catch (e: Exception) {
          errorCallback(e)
        }
      }
    }
  }
}
