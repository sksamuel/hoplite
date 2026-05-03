package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.parsers.DefaultParserRegistry
import com.sksamuel.hoplite.parsers.PropsParser
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

class ConfigFilePropertySourceTest : FunSpec({

  // Wraps PathSource so we can capture the InputStream it hands back to
  // ConfigFilePropertySource and assert that the stream is closed afterwards.
  // Reading from a closed FileInputStream / ChannelInputStream throws IOException.
  class CapturingPathSource(
    private val delegate: ConfigSource.PathSource,
    val captured: AtomicReference<InputStream> = AtomicReference()
  ) : ConfigSource() {
    override fun describe() = delegate.describe()
    override fun ext() = delegate.ext()
    override fun open(optional: Boolean) = delegate.open(optional).map { stream ->
      stream?.also { captured.set(it) }
    }
  }

  fun assertClosed(captured: AtomicReference<InputStream>) {
    val stream = captured.get() ?: error("expected ConfigFilePropertySource to open the file")
    val readAfter = runCatching { stream.read() }
    readAfter.isFailure shouldBe true
    (readAfter.exceptionOrNull() is IOException) shouldBe true
  }

  val context = PropertySourceContext(DefaultParserRegistry(mapOf("props" to PropsParser())))

  test("closes the input stream after parsing a non-empty config (#540 covered this for one path)") {
    val file = tempdir().resolve("conf.props").apply { writeText("a=1\nb=2\n") }
    val src = CapturingPathSource(ConfigSource.PathSource(file.toPath()))

    ConfigFilePropertySource(src, optional = false, allowEmpty = false).node(context)

    assertClosed(src.captured)
  }

  test("closes the input stream when the file is empty and allowEmpty=true") {
    val file = tempdir().resolve("conf.props").apply { writeText("") }
    val src = CapturingPathSource(ConfigSource.PathSource(file.toPath()))

    ConfigFilePropertySource(src, optional = false, allowEmpty = true).node(context)

    assertClosed(src.captured)
  }

  test("closes the input stream when the file is empty and allowEmpty=false (returns EmptyConfigSource)") {
    val file = tempdir().resolve("conf.props").apply { writeText("") }
    val src = CapturingPathSource(ConfigSource.PathSource(file.toPath()))

    ConfigFilePropertySource(src, optional = false, allowEmpty = false).node(context)

    assertClosed(src.captured)
  }
})
