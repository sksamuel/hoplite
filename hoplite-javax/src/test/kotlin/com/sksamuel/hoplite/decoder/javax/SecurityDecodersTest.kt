package com.sksamuel.hoplite.decoder.javax

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal

class SecurityDecodersTest : FunSpec({

  test("KerberosPrincipal decodes a valid principal") {
    data class Cfg(val p: KerberosPrincipal)
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addDecoder(KerberosPrincipalDecoder())
      .addMapSource(mapOf("p" to "alice@EXAMPLE.COM"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.p shouldBe KerberosPrincipal("alice@EXAMPLE.COM")
  }

  // viaString used to call f(node.value) directly. KerberosPrincipal's constructor throws
  // IllegalArgumentException on a bad name (e.g. missing realm), so the exception propagated
  // uncaught and crashed the loader instead of producing a clean ConfigFailure.
  test("KerberosPrincipal with an invalid name produces a ConfigException, not a raw exception") {
    data class Cfg(val p: KerberosPrincipal)
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addDecoder(KerberosPrincipalDecoder())
        .addMapSource(mapOf("p" to ""))
        .build()
        .loadConfigOrThrow<Cfg>()
    }
  }

  test("X500Principal with an invalid DN produces a ConfigException, not a raw exception") {
    data class Cfg(val p: X500Principal)
    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addDecoder(X500PrincipalDecoder())
        .addMapSource(mapOf("p" to "this is not a distinguished name"))
        .build()
        .loadConfigOrThrow<Cfg>()
    }
  }
})
