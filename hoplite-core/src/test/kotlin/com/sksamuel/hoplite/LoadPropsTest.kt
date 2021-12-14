@file:Suppress("BlockingMethodInNonBlockingContext")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.toNode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.*

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
                "c" to StringNode("wibble", pos = Pos.SourceNamePos(source = "source")),
                "d" to StringNode("123", pos = Pos.SourceNamePos(source = "source"))
              ),
              pos = Pos.SourceNamePos(source = "source"),
              value = Undefined
            ),
            "d" to StringNode("true", pos = Pos.SourceNamePos(source = "source"))
          ),
          pos = Pos.SourceNamePos(source = "source"),
          value = StringNode("foo", Pos.SourceNamePos(source = "source"))
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to StringNode("goo", pos = Pos.SourceNamePos(source = "source"))
              ),
              pos = Pos.SourceNamePos(source = "source"),
              value = StringNode("6", Pos.SourceNamePos(source = "source"))
            )
          ),
          pos = Pos.SourceNamePos(source = "source"),
          value = StringNode("5.5", Pos.SourceNamePos(source = "source"))
        )
      ),
      pos = Pos.SourceNamePos(source = "source"),
      value = Undefined
    )

    val actual = props.toNode("source")
    actual shouldBe expected
  }

})
