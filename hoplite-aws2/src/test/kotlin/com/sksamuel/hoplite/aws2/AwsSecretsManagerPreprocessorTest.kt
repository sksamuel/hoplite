package com.sksamuel.hoplite.aws2

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import java.util.Properties

class AwsSecretsManagerPreprocessorTest : FunSpec() {
  private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:3.6.0"))
    .withServices(LocalStackContainer.Service.SECRETSMANAGER)
    .withEnv("SKIP_SSL_CERT_DOWNLOAD", "true")

  init {

    install(ContainerExtension(localstack))

    val client = SecretsManagerClient.builder()
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SECRETSMANAGER))
      .credentialsProvider { AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey) }
      .build()

    test("should support secret with unquoted number when isLenient is set") {
      client.createSecret {
        it.name("unquoted")
        it.secretString("""{"port": 5432}""")
      }
      val props = Properties()
      props["port"] = "awssm://unquoted[port]"

      val json = Json { isLenient = true }
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor(json = json) { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<PortHolder>()
        .port.shouldBe(5432)
    }

    test("should support secret with quoted number for default Json serde") {
      client.createSecret {
        it.name("quoted")
        it.secretString("""{"port": "5432"}""")
      }
      val props = Properties()
      props["port"] = "awssm://quoted[port]"
      ConfigLoaderBuilder.default()
        .addPreprocessor(AwsSecretsManagerPreprocessor { client })
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<PortHolder>()
        .port.shouldBe(5432)
    }
  }

  data class PortHolder(val port: Int)
}
