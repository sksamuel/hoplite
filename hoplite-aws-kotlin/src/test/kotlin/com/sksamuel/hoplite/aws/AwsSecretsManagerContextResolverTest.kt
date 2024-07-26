package com.sksamuel.hoplite.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.parsers.PropsPropertySource
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
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class AwsSecretsManagerContextResolverTest : FunSpec() {

  private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
    .withServices(LocalStackContainer.Service.SECRETSMANAGER)
    .withEnv("SKIP_SSL_CERT_DOWNLOAD", "true")

  init {

    install(TestContainerExtension(localstack))

    val client = AWSSecretsManagerClientBuilder.standard()
      .withEndpointConfiguration(
        AwsClientBuilder.EndpointConfiguration(
          localstack.getEndpointOverride(LocalStackContainer.Service.SECRETSMANAGER).toString(),
          localstack.region
        )
      )
      .withCredentials(
        AWSStaticCredentialsProvider(
          BasicAWSCredentials(localstack.accessKey, localstack.secretKey)
        )
      ).build()

    client.createSecret(CreateSecretRequest().withName("foo").withSecretString("secret!"))
    client.createSecret(CreateSecretRequest().withName("bubble").withSecretString("""{"f": "1", "g": "2"}"""))

    test("context pattern should be detected and used") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:foo }}"
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
        .a.shouldBe("secret!")
    }

    test("prefix pattern should be detected and used") {
      val props = Properties()
      props["a"] = "aws-secrets-manager://foo"
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
        .a.shouldBe("secret!")
    }

    test("unknown secret should return error and include key") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:qwerty }}"
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfig<ConfigHolder>()
        .shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldContain("qwerty")
    }

    test("empty secret should return error and include empty secret message") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:bibblebobble }}"
      client.createSecret(CreateSecretRequest().withName("bibblebobble").withSecretString(""))
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfig<ConfigHolder>()
        .shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description().shouldContain("Empty secret")
    }

    test("unknown secret should return error and not include prefix") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:unkunk }}"
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfig<ConfigHolder>()
        .shouldBeInstanceOf<Validated.Invalid<ConfigFailure>>().error.description()
        .shouldNotContain("aws-secrets-manager://")
    }

    test("multiple errors should be returned at once") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:foo.bar }}"
      props["b"] = "\${{ aws-secrets-manager:bar.baz }}"
      shouldThrow<ConfigException> {
        ConfigLoaderBuilder.newBuilder()
          .addResolver(AwsSecretsManagerContextResolver { client })
          .addPropertySource(PropsPropertySource(props))
          .build()
          .loadConfigOrThrow<ConfigHolder2>()
      }.message.shouldContain("foo.bar").shouldContain("bar.baz")
    }

    test("should support index keys") {
      val props = Properties()
      props["a"] = "\${{ aws-secrets-manager:bubble[f] }}"
      ConfigLoaderBuilder.newBuilder()
        .addResolver(AwsSecretsManagerContextResolver { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
        .a shouldBe "1"
    }
  }

  data class ConfigHolder(val a: String)
  data class ConfigHolder2(val a: String, val b: String)
}
