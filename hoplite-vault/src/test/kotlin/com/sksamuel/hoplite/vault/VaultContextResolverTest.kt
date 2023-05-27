package com.sksamuel.hoplite.vault

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate
import org.testcontainers.utility.DockerImageName
import org.testcontainers.vault.VaultContainer
import java.util.Properties
import kotlin.collections.set

class VaultContextResolverTest : FunSpec({

  val image = DockerImageName.parse("vault:1.9.7")
  val container = VaultContainer(image)
    .withVaultToken("my-token")
    .withSecretInVault("secret/testing", "secret1=topsecret!")
    .withSecretInVault("secret/testing/nested", "secret2=bottomsecret!")

  val ext = TestContainerExtension(container)
  install(ext)

  test("prefix pattern should be detected and used") {

    val props = Properties()
    props["foo"] = "vault://secret/testing secret1"
    props["bar"] = "vault://secret/testing/nested secret2"

    val endpoint = VaultEndpoint.create(container.host, container.firstMappedPort)
    endpoint.scheme = "http" // test container doesn't use https

    val template = VaultTemplate(endpoint, TokenAuthentication("my-token"))

    val config = ConfigLoaderBuilder.default()
      .removePreprocessors()
      .addResolver(VaultContextResolver({ template }))
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<ConfigHolder>()

    config.foo.shouldBe("topsecret!")
    config.bar.shouldBe("bottomsecret!")
  }

  test("context pattern should be detected and used") {

    val props = Properties()
    props["foo"] = "hello \${{ vault:secret/testing secret1}} world"
    props["bar"] = "world \${{ vault:secret/testing/nested secret2 }} hello"

    val endpoint = VaultEndpoint.create(container.host, container.firstMappedPort)
    endpoint.scheme = "http" // test container doesn't use https

    val template = VaultTemplate(endpoint, TokenAuthentication("my-token"))

    val config = ConfigLoaderBuilder.default()
      .removePreprocessors()
      .addResolver(VaultContextResolver({ template }))
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<ConfigHolder>()

    config.foo.shouldBe("hello topsecret! world")
    config.bar.shouldBe("world bottomsecret! hello")
  }

  test("invalid vault placeholder error") {

    val props = Properties()
    props["foo"] = "vault://secret qwerty"
    props["bar"] = "vault://secret/testing/nested"

    val endpoint = VaultEndpoint.create(container.host, container.firstMappedPort)
    endpoint.scheme = "http" // test container doesn't use https

    val template = VaultTemplate(endpoint, TokenAuthentication("my-token"))

    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.default()
        .removePreprocessors()
        .addResolver(VaultContextResolver({ template }))
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
    }.message
      .shouldContain("Invalid vault path 'secret'")
      .shouldContain("Must specify vault key at 'secret/testing/nested'")
  }

  test("missing keys error message") {

    val props = Properties()
    props["foo"] = "vault://secret/testing a"
    props["bar"] = "vault://secret/testing b"

    val endpoint = VaultEndpoint.create(container.host, container.firstMappedPort)
    endpoint.scheme = "http" // test container doesn't use https

    val template = VaultTemplate(endpoint, TokenAuthentication("my-token"))

    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.default()
        .removePreprocessors()
        .addResolver(VaultContextResolver({ template }))
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
    }.message
      .shouldContain("Vault key 'a' not found in path 'secret/testing'")
      .shouldContain("Vault key 'b' not found in path 'secret/testing'")
  }

  test("missing path error message") {

    val props = Properties()
    props["foo"] = "vault://secret/foo secret1"
    props["bar"] = "vault://secret/bar secret2"

    val endpoint = VaultEndpoint.create(container.host, container.firstMappedPort)
    endpoint.scheme = "http" // test container doesn't use https

    val template = VaultTemplate(endpoint, TokenAuthentication("my-token"))

    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.default()
        .removePreprocessors()
        .addResolver(VaultContextResolver({ template }))
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<ConfigHolder>()
    }.message
      .shouldContain("Vault path 'secret/foo' not found")
      .shouldContain("Vault path 'secret/bar' not found")
  }
})
