package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class LocalTimeDecoderTest : FunSpec() {
  init {
    test("test local time") {
      data class Config(val a: LocalTime, val b: LocalTime)
      val config = ConfigLoader().loadConfigOrThrow<Config>("/localtime.props")
      config.a shouldBe LocalTime.parse("18:15")
      config.b shouldBe LocalTime.parse("18:15:59")
    }
    test("test local time with thread classloader") {
      data class Config(val a: LocalTime, val b: LocalTime)

      val config = ConfigLoader().loadConfigOrThrow<Config>(
        listOf("localtime.props"),
        Thread.currentThread().contextClassLoader.toClasspathResourceLoader()
      )
      config.a shouldBe LocalTime.parse("18:15")
      config.b shouldBe LocalTime.parse("18:15:59")
    }
  }
}
