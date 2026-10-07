package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.DefaultParserRegistry
import com.sksamuel.hoplite.parsers.Parser
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

class UserSettingsPropertySourceTest : FunSpec({

  test("closes the input stream after the parser has consumed it (#533)") {
    val home = tempdir()
    val userConfig = home.resolve(".userconfig.testext")
    userConfig.writeText("ignored")

    val captured = AtomicReference<InputStream>()
    val parser = object : Parser {
      override fun load(input: InputStream, source: String): Node {
        captured.set(input)
        input.readBytes()
        return MapNode(emptyMap(), Pos.NoPos, DotPath.root)
      }

      override fun defaultFileExtensions(): List<String> = listOf("testext")
    }
    val context = PropertySourceContext(DefaultParserRegistry(mapOf("testext" to parser)))

    val previousHome = System.getProperty("user.home")
    try {
      System.setProperty("user.home", home.absolutePath)
      UserSettingsPropertySource.node(context)
    } finally {
      if (previousHome == null) System.clearProperty("user.home") else System.setProperty("user.home", previousHome)
    }

    // After node() returns, the stream the parser saw must be closed.
    // Reading from a closed FileInputStream / ChannelInputStream throws IOException.
    val readAfter = runCatching { captured.get().read() }
    readAfter.isFailure shouldBe true
    (readAfter.exceptionOrNull() is IOException) shouldBe true
  }
})
