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

  given("an enum decoder ignoring case") {
    val decoder = EnumDecoder<TestEnum>(ignoreCase = true)

    `when`("checking it's priority") {
      val actual = decoder.priority()

      then("should return 0") {
        actual shouldBe 0
      }
    }
  }

  given("an enum decoder respecting case") {
    val decoder = EnumDecoder<TestEnum>(ignoreCase = false)

    `when`("checking it's priority") {
      val actual = decoder.priority()

      then("should return -100") {
        actual shouldBe -100
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
