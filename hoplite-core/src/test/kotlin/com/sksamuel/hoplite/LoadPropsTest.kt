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
                "c" to StringNode("wibble", pos = Pos.FilePos(source = "source"), DotPath.root),
                "d" to StringNode("123", pos = Pos.FilePos(source = "source"), DotPath.root)
              ),
              pos = Pos.FilePos(source = "source"),
              DotPath.root,
              value = Undefined,
            ),
            "d" to StringNode("true", pos = Pos.FilePos(source = "source"), DotPath.root)
          ),
          pos = Pos.FilePos(source = "source"),
          DotPath.root,
          value = StringNode("foo", Pos.FilePos(source = "source"), DotPath.root)
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to StringNode("goo", pos = Pos.FilePos(source = "source"), DotPath.root)
              ),
              pos = Pos.FilePos(source = "source"),
              DotPath.root,
              value = StringNode("6", Pos.FilePos(source = "source"), DotPath.root)
            )
          ),
          pos = Pos.FilePos(source = "source"),
          DotPath.root,
          value = StringNode("5.5", Pos.FilePos(source = "source"), DotPath.root)
        )
      ),
      pos = Pos.FilePos(source = "source"),
      DotPath.root,
      value = Undefined,
    )

    val actual = props.toNode("source")
    actual shouldBe expected
  }

})
