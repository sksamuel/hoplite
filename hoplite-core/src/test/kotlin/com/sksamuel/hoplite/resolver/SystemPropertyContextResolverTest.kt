package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.resolver.context.SystemPropertyContextResolver
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe

class SystemPropertyContextResolverTest : FunSpec({

  test("should support context matches") {

    val node = StringNode("boaty\${{sysprop:bar}}face", Pos.NoPos, DotPath.root)

    val config = withSystemProperty(key = "bar", value = "mcboat") {
      SystemPropertyContextResolver.resolve(null, node, node, DecoderContext.zero)
    }

    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }

  test("should support prefix matches") {

    val node = StringNode("sysprop://bar", Pos.NoPos, DotPath.root)

    val config = withSystemProperty(key = "bar", value = "mcboat") {
      SystemPropertyContextResolver.resolve(null, node, node, DecoderContext.zero)
    }

    (config.getUnsafe() as StringNode).value shouldBe "mcboat"
  }

  test("support leading whitespace") {

    val node = StringNode("boaty\${{  sysprop:bar}}face", Pos.NoPos, DotPath.root)

    val config = withSystemProperty(key = "bar", value = "mcboat") {
      SystemPropertyContextResolver.resolve(null, node, node, DecoderContext.zero)
    }

    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }

  test("support trailing whitespace") {

    val node = StringNode("boaty\${{  sysprop:bar    }}face", Pos.NoPos, DotPath.root)

    val config = withSystemProperty(key = "bar", value = "mcboat") {
      SystemPropertyContextResolver.resolve(null, node, node, DecoderContext.zero)
    }

    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }
})
