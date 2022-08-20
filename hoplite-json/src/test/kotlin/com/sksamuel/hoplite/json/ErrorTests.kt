package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class Wibble(val a: LocalDateTime, val b: LocalDate, val c: Instant, val d: ThreadGroup)
enum class Season { Fall }

data class Foo(val wrongType: Boolean,
               val whereAmI: String,
               val notnull: String,
               val season: Season,
               val notalist: List<String>,
               val notamap: Map<String, Boolean>,
               val notaset: Set<Long>,
               val duration: Duration,
               val nested: Wibble)

class ErrorTests : StringSpec({

  "error handling for basic errors" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/error1.json")
    }.message shouldBe """Error loading config because:

    - Could not instantiate 'com.sksamuel.hoplite.json.Foo' because:

        - 'wrongType': Required type Boolean could not be decoded from a Long value: 123 (classpath:/error1.json:2:19)

        - 'whereAmI': Missing from config

        - 'notnull': Type defined as not-null but null was loaded from config (classpath:/error1.json:6:18)

        - 'season': Required a value for the Enum type com.sksamuel.hoplite.json.Season but given value was Fun (classpath:/error1.json:8:18)

        - 'notalist': Required a List but a Boolean cannot be converted to a collection (classpath:/error1.json:3:19)

        - 'notamap': Required a Map but a Double cannot be converted to a collection (classpath:/error1.json:5:22)

        - 'notaset': Required a Set but a Long cannot be converted to a collection (classpath:/error1.json:4:17)

        - 'duration': Required type class java.time.Duration could not be decoded from a String value: 10 grams (classpath:/error1.json:7:26)

        - 'nested': - Could not instantiate 'com.sksamuel.hoplite.json.Wibble' because:

            - 'a': Required type class java.time.LocalDateTime could not be decoded from a String value: qwqwe (classpath:/error1.json:10:17)

            - 'b': Required type class java.time.LocalDate could not be decoded from a String value: qwqwe (classpath:/error1.json:11:17)

            - 'c': Required type class java.time.Instant could not be decoded from a String value: qwqwe (classpath:/error1.json:12:17)

            - 'd': Unable to locate a decoder for class java.lang.ThreadGroup"""
  }

  "error handling for resource file failures" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/weqweqweqw.json", "ewrwerwerwer.yaml")
    }.message shouldBe """Error loading config because:

    Could not find /weqweqweqw.json

    Could not find ewrwerwerwer.yaml"""
  }
})
