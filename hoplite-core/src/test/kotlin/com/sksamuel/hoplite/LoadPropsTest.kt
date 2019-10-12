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
                "c" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = "wibble"),
                "d" to MapNode(mapOf(), pos = Pos.FilePos(source = "source"), value = "123")
              ),
              pos = Pos.FilePos(source = "source"),
              value = null
            ),
            "d" to MapNode(
              mapOf(),
              pos = Pos.FilePos(source = "source"),
              value = "true"
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = "foo"
        ),
        "e" to MapNode(
          mapOf(
            "f" to MapNode(
              mapOf(
                "g" to MapNode(
                  emptyMap(),
                  pos = Pos.FilePos(source = "source"),
                  value = "goo"
                )
              ),
              pos = Pos.FilePos(source = "source"),
              value = "6"
            )
          ),
          pos = Pos.FilePos(source = "source"),
          value = "5.5"
        )
      ),
      pos = Pos.FilePos(source = "source"),
      value = null
    )

    val actual = loadProps(props, "source")
    actual shouldBe expected
  }

})
