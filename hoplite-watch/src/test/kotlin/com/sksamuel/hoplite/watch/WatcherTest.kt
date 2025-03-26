package com.sksamuel.hoplite.watch

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.framework.concurrency.eventually
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Path

class TestWatcher: Watchable {
  private var cb: (() -> Unit)? = null
  override fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    cb = callback
  }

  fun update() {
    cb?.invoke()
  }
}

data class TestConfig(val foo: String)

@ExperimentalKotest
class WatcherTest : FunSpec({

  test("should throw on startup if there is an initial error") {

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.resource("does-not-exist.yml"))

    val watcher = TestWatcher()
    shouldThrowAny {
      ReloadableConfig(configLoader.build(), TestConfig::class)
        .addWatcher(watcher)
        .getLatest()
    }
  }

  test("will call the provided error handler if reloadConfig throws on a refresh") {

    val map = mutableMapOf("foo" to "bar")
    var firsttime = true
    val onetimesource = object : PropertySource {
      override fun source(): String = "foo"
      override fun node(context: PropertySourceContext): ConfigResult<Node> {
        return if (firsttime) {
          firsttime = false
          PropertySource.map(map).node(context)
        } else {
          ConfigFailure.UnknownSource("boom").invalid()
        }
      }
    }

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(onetimesource)

    val watcher = TestWatcher()
    var error: Throwable? = null
    ReloadableConfig(configLoader.build(), TestConfig::class)
      .addWatcher(watcher)
      .withErrorHandler { error = it }

    watcher.update()

    error shouldNotBe null
    error?.message shouldContain "Error loading config"
  }

  test("will reload the config when the watchable triggers an update") {
    val map = mutableMapOf("foo" to "bar")
    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.map(map))

    val watcher = TestWatcher()
    val reloadableConfig = ReloadableConfig(configLoader.build(), TestConfig::class)
      .addWatcher(watcher)

    val config = reloadableConfig.getLatest()
    config.foo shouldBe "bar"

    map["foo"] = "baz"
    watcher.update()

    val reloadedConfig = reloadableConfig.getLatest()
    reloadedConfig shouldNotBe null
    reloadedConfig.foo shouldBe "baz"
  }

  test("FileWatcher will reload if a file in the specified directory changes") {
    val tmpFile = tempfile("file", ".json")
    tmpFile.writeText("""{"foo": "bar"}""")

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.file(tmpFile))
      .build()

    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(FileWatcher(tmpFile.parent))

    val config = reloadableConfig.getLatest()
    config.foo shouldBe "bar"

    delay(1000)
    tmpFile.writeText("""{"foo": "baz"}""")

    eventually(10000) {
      val reloadedConfig = reloadableConfig.getLatest()
      reloadedConfig shouldNotBe null
      reloadedConfig.foo shouldBe "baz"
    }
  }

  /**
   *  if its a symlink, then we should also watch symlinks for changes, this also supports mounting and watching
   *  Kubernetes-style ConfigMap resources e.g.
   *
   *  # ls -al /mnt/config/
   *  total 12
   *  drwxrwxrwx 3 root root 4096 Mar 14 08:46 .
   *  drwxr-xr-x 1 root root 4096 Mar 14 08:47 ..
   *  drwxr-xr-x 2 root root 4096 Mar 14 08:46 ..2025_03_14_08_46_21.1272616142
   *  lrwxrwxrwx 1 root root   32 Mar 14 08:46 ..data -> ..2025_03_14_08_46_21.1272616142
   *  lrwxrwxrwx 1 root root   25 Mar 14 08:46 foo.yaml -> ..data/foo.yaml
   *  lrwxrwxrwx 1 root root   20 Mar 14 08:46 bar.yaml -> ..data/bar.yaml
   *
   *  Here, Kubernetes creates a new timestamped directory when the ConfigMap changes, and just modifies the ..data symlink to point to it
   *  FileWatcher raises symlink changes (e.g. overwrite an existing link target on Linux via `ln -sfn`) as ENTRY_CREATE
   *
   */
  test("FileWatcher will reload if a file in the specified symlinked directory changes") {
    val tmpDir = tempdir("watchertest")
    val tmpDirPath = tmpDir.toPath()
    val dataDirRelPath = Path.of("..data")
    val config1RelPath = Path.of("..config1")
    val config2RelPath = Path.of("..config2")
    val configJsonRelPath = Path.of("config.json")
    val configDataDirPath = tmpDirPath.resolve(dataDirRelPath)
    val configJsonPath = tmpDirPath.resolve(configJsonRelPath)

    // k8s-style ConfigMap mounting
    val config1Path = tmpDirPath.resolve(config1RelPath)
    Files.createDirectory(config1Path)
    val tmpFile = Files.createFile(config1Path.resolve(configJsonRelPath)).toFile()
    tmpFile.writeText("""{"foo": "bar"}""")

    Files.createSymbolicLink(configDataDirPath, config1RelPath)
    Files.createSymbolicLink(configJsonPath, dataDirRelPath.resolve(configJsonRelPath))

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.file(configJsonPath.toFile()))
      .build()

    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(FileWatcher(tmpDirPath.toString()))

    val config = reloadableConfig.getLatest()
    config.foo shouldBe "bar"

    // now simulate ConfigMap symlink replacement
    val config2Path = tmpDirPath.resolve(config2RelPath)
    Files.createDirectory(config2Path)
    val tmpFile2 = Files.createFile(config2Path.resolve(configJsonRelPath)).toFile()
    tmpFile2.writeText("""{"foo": "baz"}""")

    Files.deleteIfExists(configDataDirPath)
    Files.createSymbolicLink(configDataDirPath, config2RelPath)

    delay(1000)

    eventually(10000) {
      val reloadedConfig = reloadableConfig.getLatest()
      reloadedConfig shouldNotBe null
      reloadedConfig.foo shouldBe "baz"
    }
  }
})
