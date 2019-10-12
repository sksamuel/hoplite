package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.loadProps
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
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
                "c" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = Value.StringNode("wibble")),
                "d" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = Value.StringNode("123"))
              ),
              pos = Pos.FilePos(source = "source"),
              value = Value.NullValue
            ),
            "d" to MapNode(
              mapOf(),
              pos = Pos.FilePos(source = "source"),
              value = Value.StringNode("true")
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = Value.StringNode("foo")
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to MapNode(
                  emptyMap(),
                  pos = Pos.FilePos(source = "source"),
                  value = Value.StringNode("goo")
                )
              ),
              pos = Pos.FilePos(source = "source"),
              value = Value.StringNode("6")
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = Value.StringNode("5.5")
        )
      ),
      pos = Pos.FilePos(source = "source"),
      value = Value.NullValue
    )

    val actual = loadProps(props, "source")
    actual shouldBe expected
  }

})
