package com.sksamuel.hoplite.aws

import com.amazonaws.SdkClientException
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

class ParameterStorePreprocessorTest : FunSpec() {

  init {

    test("prefix should be detected and used") {
      shouldThrow<SdkClientException> {
        ParameterStorePreprocessor.process(StringNode("\${ssm:/foo}", Pos.NoPos))
      }
    }

    test("loading yml should use processor") {
      shouldThrow<SdkClientException> {
        ConfigLoader().withPreprocessor(ParameterStorePreprocessor).loadConfigOrThrow<ConfigHolder>("/ssm.props")
      }
    }
  }

  data class ConfigHolder(val a: String)
}
