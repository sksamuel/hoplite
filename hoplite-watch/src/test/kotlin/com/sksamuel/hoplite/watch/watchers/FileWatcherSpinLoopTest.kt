package com.sksamuel.hoplite.watch.watchers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FileWatcherSpinLoopTest : FunSpec({

  // Regression for #558. Before the fix, FileWatcher's loop caught Exception generically,
  // invoked errorCallback, and continued. ClosedWatchServiceException is thrown by every
  // subsequent take() once the service is closed, so the loop spun as fast as the JVM
  // could run the callback — burning CPU and flooding the user's error handler.
  test("FileWatcher exits cleanly (no spin) when the watch service is closed externally") {
    val tmpDir = tempdir()
    val errorCount = AtomicInteger(0)

    val watcher = FileWatcher(tmpDir.absolutePath)
    watcher.watch(callback = {}, errorHandler = { errorCount.incrementAndGet() })

    // Close the service externally — that's the trigger for ClosedWatchServiceException.
    watcher.watchService.close()

    // The worker thread should observe the close on its next take() and break out of the
    // loop. Give it a moment, then assert the future completes (the worker exited).
    watcher.future.get(2, TimeUnit.SECONDS)
    watcher.future.isDone shouldBe true

    // Without the fix, errorCallback would have fired hundreds or thousands of times in
    // those two seconds. With the fix it should fire at most once (and ideally zero — we
    // break before invoking it). Allow up to 1 to leave a tiny tolerance for ordering.
    errorCount.get() shouldBeLessThanOrEqualTo 1
  }
})
