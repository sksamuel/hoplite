package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.Validated
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

class AwsSecretsManagerPreprocessorTest : FunSpec() {

  private val localstackImage = DockerImageName.parse("localstack/localstack:0.14.1")
  private val localstack = LocalStackContainer(localstackImage).withServices(LocalStackContainer.Service.SECRETSMANAGER)

  init {

    install(TestContainerExtension(localstack))

    val client = AWSSecretsManagerClientBuilder.standard()
      .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SECRETSMANAGER))
      .withCredentials(localstack.defaultCredentialsProvider)
      .build()

    test("placeholder should be detected and used") {

      client.createSecret(CreateSecretRequest().withName("foo").withSecretString("secret!"))

      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .build()
        .loadConfigOrThrow<ConfigHolder>("/secrets.props")
        .a.shouldBe("secret!")
    }

    test("unknown secret should return error and include key") {
      AwsSecretsManagerPreprocessor { client }.process(StringNode("secretsmanager://unkunk", Pos.NoPos, DotPath.root))
        .shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldContain("unkunk")
    }

    test("unknown secret should return error and not include prefix") {
      AwsSecretsManagerPreprocessor { client }.process(StringNode("secretsmanager://unkunk", Pos.NoPos, DotPath.root))
        .shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldNotContain("secretsmanager://")
    }

    test("multiple errors should be returned at once") {
      shouldThrow<ConfigException> {
        ConfigLoaderBuilder.default()
          .addPreprocessor(AwsSecretsManagerPreprocessor { client })
          .build()
          .loadConfigOrThrow<ConfigHolder>("/multiple_secrets.props")
      }.message.shouldContain("foo.bar").shouldContain("bar.baz")
    }
  }

  data class ConfigHolder(val a: String)
}
