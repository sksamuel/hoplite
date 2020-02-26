package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

data class Parser(val regex: Regex, val id: String)
data class Supplier(val name: String, val code: String, val parsers: List<Parser>)
data class Foo(val suppliers: List<Supplier>)

class CollectionErrorTests : StringSpec({

  "error handling for collection errors" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/collections_error.yaml")
    }.message shouldBe """Error loading config because:

    - Could not instantiate 'com.sksamuel.hoplite.yaml.Foo' because:

        - 'suppliers': Collection element decode failure (/collections_error.yaml:1:2):

            - Could not instantiate 'com.sksamuel.hoplite.yaml.Supplier' because:

                - 'parsers': Collection element decode failure (/collections_error.yaml:4:7):

                    - Could not instantiate 'com.sksamuel.hoplite.yaml.Parser' because:

                        - 'id': Missing from config

            - Could not instantiate 'com.sksamuel.hoplite.yaml.Supplier' because:

                - 'parsers': Collection element decode failure (/collections_error.yaml:10:7):

                    - Could not instantiate 'com.sksamuel.hoplite.yaml.Parser' because:

                        - 'id': Missing from config

            - Could not instantiate 'com.sksamuel.hoplite.yaml.Supplier' because:

                - 'parsers': Collection element decode failure (/collections_error.yaml:16:7):

                    - Could not instantiate 'com.sksamuel.hoplite.yaml.Parser' because:

                        - 'id': Missing from config"""
  }
})
