package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.PropsPropertySource
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class SubstitutionResolverTest : FunSpec({

  test("should find matches") {

    val node = StringNode("boaty\${{ref:bar}}face", Pos.NoPos, DotPath.root)

    val props = Properties()
    props["bar"] = "mcboat"
    val root = PropsPropertySource(props).node(PropertySourceContext(defaultParserRegistry())).getUnsafe()

    val config = ReferenceContextResolver.resolve(node, root, DecoderContext.zero)
    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }

  test("support leading whitespace") {

    val props = Properties()
    props["bar"] = "mcboat"
    val root = PropsPropertySource(props).node(PropertySourceContext(defaultParserRegistry())).getUnsafe()
    val node = StringNode("boaty\${{  ref:bar}}face", Pos.NoPos, DotPath.root)

    val config = ReferenceContextResolver.resolve(node, root, DecoderContext.zero)
    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }

  test("support trailing whitespace") {

    val props = Properties()
    props["bar"] = "mcboat"
    val root = PropsPropertySource(props).node(PropertySourceContext(defaultParserRegistry())).getUnsafe()
    val node = StringNode("boaty\${{  ref:bar    }}face", Pos.NoPos, DotPath.root)

    val config = ReferenceContextResolver.resolve(node, root, DecoderContext.zero)
    (config.getUnsafe() as StringNode).value shouldBe "boatymcboatface"
  }
})
