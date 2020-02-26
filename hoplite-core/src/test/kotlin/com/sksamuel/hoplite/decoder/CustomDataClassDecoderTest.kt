package com.sksamuel.hoplite.decoder

import arrow.core.valid
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class CustomDataClassDecoderTest : FunSpec({

  data class Foo(val a: String)

  val fooDecoder = object : Decoder<Foo> {

    override fun supports(type: KType): Boolean {
      return type.classifier == Foo::class
    }

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Foo> {
      return Foo("wibble").valid()
    }
  }

  test("allow custom data class decoders to override existing") {


    defaultDecoderRegistry()
      .register(fooDecoder)
      .decoder(Foo::class)
      .fold(
        { fail("error") },
        { decoder ->
          decoder.decode(NullNode(Pos.NoPos), Foo::class.createType(), DecoderContext.zero).fold(
            { fail("error") },
            { it.a shouldBe "wibble" }
          )
        }
      )
  }

})

