package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest
import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.parsers.PropsPropertySource
import com.sksamuel.hoplite.traverse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import java.util.Properties

class AwsSecretsManagerPreprocessorTest : FunSpec() {

  private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
    .withServices(LocalStackContainer.Service.SECRETSMANAGER)
    .withEnv("SKIP_SSL_CERT_DOWNLOAD", "true")

  init {

    install(TestContainerExtension(localstack))

    val client = AWSSecretsManagerClientBuilder.standard()
      .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SECRETSMANAGER))
      .withCredentials(localstack.defaultCredentialsProvider)
      .build()

    client.createSecret(CreateSecretRequest().withName("foo").withSecretString("secret!"))
    client.createSecret(CreateSecretRequest().withName("bubble").withSecretString("""{"f": "1", "g": "2"}"""))

    test("placeholder should be detected and used") {
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .build()
        .loadConfigOrThrow<ConfigHolder>("/secrets.props")
        .a.shouldBe("secret!")
    }

    test("node should be annotated with IsDecodedSecret attribute") {
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .build()
        .loadNodeOrThrow("/secrets.props")
        .traverse()
        .find { it.path == DotPath("a") }
        .shouldNotBeNull().meta[CommonMetadata.Secret] shouldBe true
    }

    test("node should be annotated with UnprocessedValue attribute") {
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .build()
        .loadNodeOrThrow("/secrets.props")
        .traverse()
        .find { it.path == DotPath("a") }
        .shouldNotBeNull().meta[CommonMetadata.UnprocessedValue] shouldBe "awssm://foo"
    }

    test("unknown secret should return error and include key") {
      AwsSecretsManagerPreprocessor { client }.process(
        StringNode(
          "secretsmanager://unkunk",
          Pos.NoPos,
          DotPath.root,
          emptyMap()
        ),
        DecoderContext.zero,
      ).shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldContain("unkunk")
    }

    test("empty secret should return error and include key") {
      client.createSecret(CreateSecretRequest().withName("bibblebobble").withSecretString(""))
      AwsSecretsManagerPreprocessor { client }.process(
        StringNode(
          "secretsmanager://bibblebobble",
          Pos.NoPos,
          DotPath.root,
          emptyMap(),
        ),
        DecoderContext.zero,
      ).shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldContain("Empty secret")
    }

    test("unknown secret should return error and not include prefix") {
      AwsSecretsManagerPreprocessor { client }.process(
        StringNode(
          "secretsmanager://unkunk", Pos.NoPos, DotPath.root,
          emptyMap()
        ),
        DecoderContext.zero,
      ).shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description()
        .shouldNotContain("secretsmanager://")
    }

    test("multiple errors should be returned at once") {
      shouldThrow<ConfigException> {
        ConfigLoaderBuilder.default()
          .addPreprocessor(AwsSecretsManagerPreprocessor { client })
          .build()
          .loadConfigOrThrow<ConfigHolder>("/multiple_secrets.props")
      }.message.shouldContain("foo.bar").shouldContain("bar.baz")
    }

    test("should support index keys") {
      val props = Properties()
      props["a"] = "awssm://bubble[f]"
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
        .a shouldBe "1"
    }
  }

  data class ConfigHolder(val a: String)
}
