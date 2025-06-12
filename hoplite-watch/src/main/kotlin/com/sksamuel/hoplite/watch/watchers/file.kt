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
    // we don't use toRealPath() here, because we *want* to watch the symlinked parent the file is in, not the file's real parent
    val pathToWatch = Paths.get(dir).toAbsolutePath()

    pathToWatch.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE
    )

    Executors.newSingleThreadExecutor { Thread(it).apply { isDaemon = true } }.submit {
      while (true) {
        try {
          val watchKey = watchService.take()
          if (watchKey != null) {
            val events = watchKey.pollEvents()
            if (events.isNotEmpty()) {
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
