@file:Suppress("BlockingMethodInNonBlockingContext")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.toNode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class LoadPropsTest : FunSpec({

  test("loading props from file") {

    val props = Properties()
    props.load(javaClass.getResourceAsStream("/advanced.props"))

    val expected = MapNode(
      mapOf(
        "a" to MapNode(
          mapOf(
            "b" to MapNode(
              mapOf(
                "c" to StringNode("wibble", pos = Pos.SourcePos(source = "source"), DotPath("a", "b", "c")),
                "d" to StringNode("123", pos = Pos.SourcePos(source = "source"), DotPath("a", "b", "d"))
              ),
              pos = Pos.SourcePos(source = "source"),
              DotPath("a", "b"),
              value = Undefined,
            ),
            "d" to StringNode("true", pos = Pos.SourcePos(source = "source"), DotPath("a", "d"))
          ),
          pos = Pos.SourcePos(source = "source"),
          DotPath("a"),
          value = StringNode("foo", Pos.SourcePos(source = "source"), DotPath("a"))
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to StringNode("goo", pos = Pos.SourcePos(source = "source"), DotPath("e", "f", "g"))
              ),
              pos = Pos.SourcePos(source = "source"),
              DotPath("e", "f"),
              value = StringNode("6", Pos.SourcePos(source = "source"), DotPath("e", "f"))
            )
          ),
          pos = Pos.SourcePos(source = "source"),
          DotPath("e"),
          value = StringNode("5.5", Pos.SourcePos(source = "source"), DotPath("e"))
        )
      ),
      pos = Pos.SourcePos(source = "source"),
      DotPath.root,
      value = Undefined,
    )

    val actual = props.toNode("source")
    actual shouldBe expected
  }

})
