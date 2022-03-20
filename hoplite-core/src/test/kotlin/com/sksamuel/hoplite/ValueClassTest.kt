package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.full.primaryConstructor

@JvmInline
value class Port(val value: Int)

data class Config1(val port: Port)
data class Config2(val port: Port?)

class ValueClassTest : FunSpec() {
  init {

    test("value classes should be supported") {
      ConfigLoaderBuilder
        .default()
        .addPropertySource(PropertySource.string("port = 1234", "props"))
        .build()
        .loadConfigOrThrow<Config1>()
        .port.value shouldBe 1234
    }

    test("nullable value classes should be supported") {

      Config1::class.primaryConstructor!!.call(Port(123))
      Config2::class.primaryConstructor!!.call(Port(123))

//      ConfigLoaderBuilder
//        .default()
//        .addPropertySource(PropertySource.string("port = 1234", "props"))
//        .build()
//        .loadConfigOrThrow<Config2>()
//        .port!!.value shouldBe 1234
    }
  }
}
