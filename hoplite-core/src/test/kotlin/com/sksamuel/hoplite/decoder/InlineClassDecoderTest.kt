package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.defaultNodeTransformers
import com.sksamuel.hoplite.defaultParamMappers
import com.sksamuel.hoplite.fp.Validated
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.full.createType

class InlineClassDecoderTest : StringSpec({

    "should handle illegal argument exceptions gracefully" {
        val node = LongNode(-1, Pos.NoPos, DotPath.root)

        InlineClassDecoder().decode(
            node, Port::class.createType(),
            DecoderContext(defaultDecoderRegistry(), defaultParamMappers(), defaultNodeTransformers())
        )
            .shouldBeInstanceOf<Validated.Invalid<ConfigFailure.InvalidConstructorParameters>>()
            .error.e.shouldBeInstanceOf<IllegalArgumentException>()
            .message shouldBe "Invalid port: -1"
    }
}) {
    @JvmInline
    value class Port(val value: Int) {
        init {
            require(value in 0..65535) { "Invalid port: $value" }
        }
    }
}
