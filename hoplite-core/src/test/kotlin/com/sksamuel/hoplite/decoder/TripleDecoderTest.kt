package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.typeOf

class TripleDecoderTest : StringSpec({

  "triple should be decoded from string with 3 fields" {
    data class Test(val a: Triple<String, String, Boolean>, val b: Triple<String, Long, Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_triple.props")
    config shouldBe Test(Triple("hello", "world", true), Triple("5", 4L, 3))
  }

  "triple should trim whitespace around comma-separated string elements" {
    data class Test(val a: Triple<String, Long, Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_triple_whitespace.props")
    config shouldBe Test(Triple("hello", 4L, 3))
  }

  "triple should fail with helpful message when array has wrong number of elements" {
    val pos = Pos.NoPos
    val path = DotPath.root
    val node = ArrayNode(
      elements = listOf(
        StringNode("a", pos, path),
        StringNode("b", pos, path),
      ),
      pos = pos,
      path = path,
    )
    val result = TripleDecoder().safeDecode(
      node,
      typeOf<Triple<String, String, String>>(),
      DecoderContext.zero,
    )
    result.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
    result.error.description() shouldContain "Triple requires a list of three elements"
  }
})
