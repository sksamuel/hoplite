package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.loadProps
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.util.*

class LoadPropsTest : FunSpec({

  test("loading props from file") {

    val props = Properties()
    props.load(javaClass.getResourceAsStream("/advanced.props"))

    val expected = MapValue(
      mapOf(
        "a" to MapValue(
          mapOf(
            "b" to MapValue(
              mapOf(
                "c" to MapValue(mapOf(), pos = Pos.FilePos(source = "source"), dotpath = "<root>", value = "wibble"),
                "d" to MapValue(mapOf(), pos = Pos.FilePos(source = "source"), dotpath = "<root>", value = "123")
              ),
              pos = Pos.FilePos(source = "source"),
              dotpath = "<root>",
              value = null
            ),
            "d" to MapValue(
              mapOf(),
              pos = Pos.FilePos(source = "source"),
              dotpath = "<root>",
              value = "true"
            )
          ),
          pos = Pos.FilePos(source = "source"),
          dotpath = "<root>",
          value = "foo"
        ),
        "e" to MapValue(
          mapOf(
            "f" to MapValue(
              mapOf(
                "g" to MapValue(
                  emptyMap(),
                  pos = Pos.FilePos(source = "source"),
                  dotpath = "<root>",
                  value = "goo"
                )
              ),
              pos = Pos.FilePos(source = "source"),
              dotpath = "<root>",
              value = "6"
            )
          ),
          pos = Pos.FilePos(source = "source"),
          dotpath = "<root>",
          value = "5.5"
        )
      ),
      pos = Pos.FilePos(source = "source"),
      dotpath = "<root>",
      value = null
    )

    val actual = loadProps(props, "source")
    actual shouldBe expected
  }

})
