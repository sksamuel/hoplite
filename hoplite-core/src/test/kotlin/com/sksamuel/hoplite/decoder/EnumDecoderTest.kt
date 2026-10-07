package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigEnumDefault
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderConfig
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.defaultNodeTransformers
import com.sksamuel.hoplite.defaultParamMappers
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EnumDecoderTest : BehaviorSpec({

  given("a value matching enum case") {
    val node = StringNode("ONE", Pos.NoPos, DotPath.root)

    `when`("using case sensitive decoding") {
      val actual = EnumDecoder<TestEnum>().decode(node, ignoreCase = false)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.ONE
      }
    }

    `when`("using case insensitive decoding") {
      val actual = EnumDecoder<TestEnum>().decode(node, ignoreCase = true)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.ONE
      }
    }
  }

  given("a value not matching enum case") {
    val node = StringNode("Two", Pos.NoPos, DotPath.root)

    `when`("using case sensitive decoding") {
      val actual = EnumDecoder<TestEnum>().decode(node, ignoreCase = false)

      then("should return error") {
        actual.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
      }
    }

    `when`("using case insensitive decoding") {
      val actual = EnumDecoder<TestEnum>().decode(node, ignoreCase = true)

      then("should decode it") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnum>>()
          .value shouldBe TestEnum.TWO
      }
    }
  }

  given("an enum class annotated with @ConfigEnumDefault") {
    `when`("the configured value does not match any constant") {
      val node = StringNode("Yellow", Pos.NoPos, DotPath.root)
      val actual = EnumDecoder<TestEnumWithDefault>().decode(node, TestEnumWithDefault::class.createType())

      then("it should fall back to the default constant") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnumWithDefault>>()
          .value shouldBe TestEnumWithDefault.Unknown
      }
    }

    `when`("the configured value matches a real constant") {
      val node = StringNode("Red", Pos.NoPos, DotPath.root)
      val actual = EnumDecoder<TestEnumWithDefault>().decode(node, TestEnumWithDefault::class.createType())

      then("it should decode normally without using the fallback") {
        actual.shouldBeInstanceOf<Validated.Valid<TestEnumWithDefault>>()
          .value shouldBe TestEnumWithDefault.Red
      }
    }
  }

  given("an enum class annotated with @ConfigEnumDefault naming a missing constant") {
    `when`("the configured value does not match any constant") {
      val node = StringNode("Yellow", Pos.NoPos, DotPath.root)
      val actual = EnumDecoder<TestEnumWithBadDefault>().decode(node, TestEnumWithBadDefault::class.createType())

      then("it should report the original invalid-enum failure") {
        actual.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
      }
    }
  }

  // Regression: previously the decoder matched against Enum.toString(); a custom toString()
  // (very common when the display form differs from the constant name) would break the lookup.
  given("an enum class with an overridden toString()") {
    `when`("the configured value matches the canonical enum name") {
      val node = StringNode("RED", Pos.NoPos, DotPath.root)
      val actual = EnumDecoder<EnumWithCustomToString>().decode(node, EnumWithCustomToString::class.createType())

      then("it should still resolve via Enum.name and ignore the custom toString()") {
        actual.shouldBeInstanceOf<Validated.Valid<EnumWithCustomToString>>()
          .value shouldBe EnumWithCustomToString.RED
      }
    }

    `when`("the configured value matches only the custom toString() (but not the name)") {
      val node = StringNode("Color::red", Pos.NoPos, DotPath.root)
      val actual = EnumDecoder<EnumWithCustomToString>().decode(node, EnumWithCustomToString::class.createType())

      then("it should fail — the custom toString() is for display only") {
        actual.shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>()
      }
    }
  }
}) {
  private companion object {
    fun <T : Any> EnumDecoder<T>.decode(node: PrimitiveNode, ignoreCase: Boolean) = decode(
      node,
      TestEnum::class.createType(),
      DecoderContext(
        decoders = defaultDecoderRegistry(),
        paramMappers = defaultParamMappers(),
        nodeTransformers = defaultNodeTransformers(),
        config = DecoderConfig(flattenArraysToString = false, resolveTypesCaseInsensitive = ignoreCase)
      )
    )

    fun <T : Any> EnumDecoder<T>.decode(node: PrimitiveNode, type: KType) = decode(
      node,
      type,
      DecoderContext(
        decoders = defaultDecoderRegistry(),
        paramMappers = defaultParamMappers(),
        nodeTransformers = defaultNodeTransformers(),
        config = DecoderConfig(flattenArraysToString = false, resolveTypesCaseInsensitive = false)
      )
    )

    enum class TestEnum {
      ONE, TWO
    }

    @ConfigEnumDefault("Unknown")
    enum class TestEnumWithDefault {
      Red, Blue, Green, Unknown
    }

    @ConfigEnumDefault("DoesNotExist")
    enum class TestEnumWithBadDefault {
      Red, Blue
    }

    enum class EnumWithCustomToString {
      RED, GREEN, BLUE;
      override fun toString(): String = "Color::${name.lowercase()}"
    }
  }
}
