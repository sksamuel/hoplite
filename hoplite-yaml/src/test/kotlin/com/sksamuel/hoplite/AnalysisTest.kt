package com.sksamuel.hoplite

import com.sksamuel.hoplite.secrets.DefaultSecretStrengthAnalyzer
import com.sksamuel.hoplite.secrets.StandardSecretsPolicy
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.core.spec.style.FunSpec

class AnalysisTest : FunSpec() {
  init {
    test("analysis should detect weak passwords") {
      data class Foo(
        val host: String,
        val password: String,
        val secret: String,
        val credentials: String,
      )
      ConfigLoader.builder()
        .addSource(YamlPropertySource("host: localhost"))
        .addSource(YamlPropertySource("password: bubble"))
        .addSource(YamlPropertySource("secret: bubblebobble"))
        .addSource(YamlPropertySource("credentials: Bubble123bobble$$"))
        .addSource(YamlPropertySource("unused: foo"))
        .withReport()
        .withSecretsPolicy(StandardSecretsPolicy)
        .withSecretStrengthAnalyzer(DefaultSecretStrengthAnalyzer)
        .build()
        .loadConfigOrThrow<Foo>()
    }
  }
}
