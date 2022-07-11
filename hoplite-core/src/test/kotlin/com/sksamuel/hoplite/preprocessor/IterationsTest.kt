package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class IterationsTest : FunSpec() {
  init {
    test("preprocessing iterations > 1") {
      data class Config(val a: String)

      val preprocessor = object : TraversingPrimitivePreprocessor() {
        override fun handle(node: PrimitiveNode): ConfigResult<Node> {
          return when (node) {
            is StringNode -> node.copy(value = node.value + "a").valid()
            else -> node.valid()
          }
        }
      }

      val props = Properties()
      props["a"] = "foo"

      val config = ConfigLoaderBuilder.default()
        .withPreprocessingIterations(3)
        .addPreprocessor(preprocessor)
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<Config>()

      config.a shouldBe "fooaaa"
    }
  }
}
