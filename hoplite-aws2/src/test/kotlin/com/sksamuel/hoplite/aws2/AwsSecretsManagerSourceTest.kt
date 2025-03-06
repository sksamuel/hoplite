package com.sksamuel.hoplite.aws2

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import java.util.Properties

class AwsSecretsManagerSourceTest : FunSpec() {
  private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:3.6.0"))
    .withServices(LocalStackContainer.Service.SECRETSMANAGER)
    .withEnv("SKIP_SSL_CERT_DOWNLOAD", "true")

  init {

    install(ContainerExtension(localstack))

    val client = SecretsManagerClient.builder()
      .region(Region.US_EAST_1)
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SECRETSMANAGER))
      .credentialsProvider { AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey) }
      .build()

    test("should support secret with unquoted number when isLenient is set") {
      client.createSecret {
        it.name("unquoted")
        it.secretString("""{"key1": 222, "key2": "override2"}""")
      }
      val props = Properties()
      props["key2"] = "original2"

      val json = Json { isLenient = true }
      ConfigLoaderBuilder.default()
        .addMapSource(AwsSecretsManagerSource(json = json) { client }.fetchSecretAsMap("unquoted"))
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<PortHolder>().apply {
          key1.shouldBe(222)
          key2.shouldBe("override2")
        }

    }

    test("should support secret with quoted number for default") {
      client.createSecret {
        it.name("quoted")
        it.secretString("""{"key1": "222", "key2": "override2"}""")
      }
      val props = Properties()
      props["key2"] = "original2"
      ConfigLoaderBuilder.default()
        .addMapSource(AwsSecretsManagerSource { client }.fetchSecretAsMap("quoted"))
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<PortHolder>().apply {
          key1.shouldBe(222)
          key2.shouldBe("override2")
        }
    }
  }

  data class PortHolder(val key1: Int, val key2: String)
}
