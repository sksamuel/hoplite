package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.defaultParamMappers
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.full.createType

class EnumDecoderTest : BehaviorSpec({

  given("a value matching enum case") {
    val node = StringNode("ONE", Pos.NoPos, DotPath.root)

    `when`("using case sensitive decoding") {
      val actual = EnumDecoder<TestEnum>(ignoreCase = false).decode(node)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.ONE
      }
    }

    `when`("using case insensitive decoding") {
      val actual = EnumDecoder<TestEnum>(ignoreCase = true).decode(node)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.ONE
      }
    }
  }

  given("a value not matching enum case") {
    val node = StringNode("Two", Pos.NoPos, DotPath.root)

    `when`("using case sensitive decoding") {
      val actual = EnumDecoder<TestEnum>(ignoreCase = false).decode(node)

      then("should return error") {
        actual.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
      }
    }

    `when`("using case insensitive decoding") {
      val actual = EnumDecoder<TestEnum>(ignoreCase = true).decode(node)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.TWO
      }
    }
  }
}) {
  private companion object {
    fun <T : Any> EnumDecoder<T>.decode(node: PrimitiveNode) = decode(
      node,
      TestEnum::class.createType(),
      DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
    )

    enum class TestEnum {
      ONE, TWO
    }
  }
}
