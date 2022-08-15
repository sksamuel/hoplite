package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Masked
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

sealed class ClientAuthConfig {
  data class UrlUserPass(val url: String, val user: String, val password: Masked) : ClientAuthConfig()

  data class UrlWithWithDefaultValue(val otherStuff: String = "", val anotherStuff: String = "") : ClientAuthConfig()
  data class Url(val url: String) : ClientAuthConfig()
}

data class DbConfig(
  val clientAuth: ClientAuthConfig,
  val clientNoAuth: ClientAuthConfig = ClientAuthConfig.UrlWithWithDefaultValue(),
  val clientWithSpecifiedValues: ClientAuthConfig,
  val clientWithDefaultValues: ClientAuthConfig = ClientAuthConfig.UrlWithWithDefaultValue(),
)

class SealedClassTest : FunSpec() {
  init {
    test("sealed classes should pick the most specific") {
      val config = ConfigLoader().loadConfigOrThrow<DbConfig>("/sealed.conf")
      config.clientAuth shouldBe ClientAuthConfig.UrlUserPass(
        url = "theClientUrl",
        user = "2user",
        password = Masked("3pass"),
      )
      config.clientNoAuth shouldBe ClientAuthConfig.Url(url = "1url")
      config.clientWithSpecifiedValues shouldBe ClientAuthConfig.UrlWithWithDefaultValue(
        otherStuff = "1url",
        anotherStuff = "test"
      )
      config.clientWithDefaultValues shouldBe ClientAuthConfig.UrlWithWithDefaultValue()
    }
  }
}
