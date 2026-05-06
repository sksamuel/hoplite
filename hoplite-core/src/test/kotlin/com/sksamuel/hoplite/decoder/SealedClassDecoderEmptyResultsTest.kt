package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.addMapSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SealedClassDecoderEmptyResultsTest : FunSpec({

  // Regression: deriveInstance previously called `results.sequence().getInvalidUnsafe()` after
  // filtering out subclasses whose mandatory-arg count exceeded the supplied node's size. If
  // the filter dropped *every* subclass (e.g. node has one field but every subclass needs two
  // mandatory args), `results` was empty, `sequence()` returned `Valid(emptyList)`, and
  // `getInvalidUnsafe()` threw IllegalStateException("Not an invalid instance...") — leaking
  // an internal stack trace instead of a clean SealedClassSubtypeFailure.
  test("undecodable sealed config produces a clean failure, not IllegalStateException") {
    val ex = shouldThrow<ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        // single-field map can't satisfy any of the two-mandatory-arg subclasses
        .addMapSource(mapOf("animal" to mapOf("name" to "Whiskers")))
        .build()
        .loadConfigOrThrow<Holder>()
    }

    // Before the fix: message contained "Not an invalid instance" or similar IllegalStateException text.
    ex.message.orEmpty() shouldNotContain "Not an invalid instance"
    // After the fix: a recognisable sealed-class failure mentioning the type.
    ex.message.orEmpty() shouldContain "Animal"
  }
}) {
  sealed interface Animal {
    data class Cat(val name: String, val age: Int) : Animal
    data class Dog(val breed: String, val owner: String) : Animal
  }

  data class Holder(val animal: Animal)
}
