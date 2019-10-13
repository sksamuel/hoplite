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
                "c" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = StringNode("wibble", Pos.NoPos)),
                "d" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = StringNode("123", Pos.NoPos))
              ),
              pos = Pos.FilePos(source = "source"),
              value = NullValue(Pos.NoPos)
            ),
            "d" to MapNode(
              mapOf(),
              pos = Pos.FilePos(source = "source"),
              value = StringNode("true", Pos.NoPos)
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = StringNode("foo", Pos.NoPos)
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to MapNode(
                  emptyMap(),
                  pos = Pos.FilePos(source = "source"),
                  value = StringNode("goo", Pos.NoPos)
                )
              ),
              pos = Pos.FilePos(source = "source"),
              value = StringNode("6", Pos.NoPos)
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = StringNode("5.5", Pos.NoPos)
        )
      ),
      pos = Pos.FilePos(source = "source"),
      value = NullValue(Pos.NoPos)
    )

    val actual = loadProps(props, "source")
    actual shouldBe expected
  }

})
