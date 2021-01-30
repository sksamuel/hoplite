package hoplite.decoder

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class CustomDataClassDecoderTest : FunSpec({

  data class Foo(val a: String)

  val fooDecoder = object : NullHandlingDecoder<Foo> {

    override fun supports(type: KType): Boolean {
      return type.classifier == Foo::class
    }

    override fun priority(): Int = 0

    override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<Foo> {
      return Foo("wibble").valid()
    }
  }

  test("allow custom data class decoders to override existing") {
    val registry = defaultDecoderRegistry().register(fooDecoder)
    registry.decoder(Foo::class)
      .fold(
        { fail("error finding decoder ") },
        { decoder ->
          decoder.decode(StringNode("qqqq", Pos.NoPos), Foo::class.createType(), DecoderContext.zero).fold(
            { fail("error decoding ${it.description()}") },
            { it.a shouldBe "wibble" }
          )
        }
      )
  }
})

