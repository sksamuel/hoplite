package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.FunSpec
import java.util.Properties
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

// https://github.com/sksamuel/hoplite/issues/389
class Github389 : FunSpec() {
  init {
    test("NullPointerException on Config load #389") {
      val props = Properties()
      props["someInt"] = 1
      props["test"] = "a"
      ConfigLoaderBuilder.default()
        .addPropertySource(PropsPropertySource(props))
        .addDecoder(MyDecoder)
        .build()
        .loadConfigOrThrow<MyConfig>()
    }
  }

}

object MyDecoder : Decoder<MyClass> {
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<MyClass> =
    when (node) {
      is StringNode -> MyClass(node.value).valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }

  override fun supports(type: KType): Boolean = type.isSubtypeOf(MyClass::class.starProjectedType)
}

class MyClass(val value: String) {
  override fun hashCode(): Int = error("do not run at config loadtime")
}

data class MyConfig(
  val someInt: Int,
  val test: MyClass,
)
