package com.sksamuel.hoplite.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Tuple4
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.addMapSource
import com.sksamuel.hoplite.secrets.SecretTypeSecretsPolicy
import com.sksamuel.hoplite.secrets.SecretsPolicy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@OptIn(ExperimentalHoplite::class)
class OptionTest : FunSpec({

  test("support Options") {
    data class Foo(val a: Option<String>,
                   val b: Option<Long>,
                   val c: Option<String>,
                   val d: Option<Long>,
                   val e: Option<UUID>)

    val foo = ConfigLoader().loadConfigOrThrow<Foo>("/options.yml")
    foo.a shouldBe None
    foo.b shouldBe None
    foo.c shouldBe Some("hello")
    foo.d shouldBe Some(123L)
    foo.e shouldBe Some(UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d"))
  }

  // OptionDecoder used to enumerate only the primitive node types (StringNode, LongNode, …),
  // so Option<DataClass> and Option<List<X>> failed with DecodeError despite the inner decoders
  // being perfectly capable. Defer to the inner decoder for non-null/undefined nodes.
  test("Option<DataClass> decodes a present nested data class") {
    data class Inner(val name: String, val count: Int)
    data class Outer(val inner: Option<Inner>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("inner.name" to "rimmer", "inner.count" to "3"))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.inner shouldBe Some(Inner("rimmer", 3))
  }

  test("Option<List<String>> decodes a present list") {
    data class Outer(val xs: Option<List<String>>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("xs" to listOf("a", "b", "c")))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.xs shouldBe Some(listOf("a", "b", "c"))
  }

  // gh-613: the report printed the values of Option<Secret> and tuples of secrets in plain text.
  test("report should mask Option<Secret> and tuple elements of type Secret") {
    data class Test(val option: Option<Secret>, val tuple: Tuple4<String, String, String, Secret>)

    val report = StringBuilder()
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(
        PropertySource.map(mapOf("option" to "option-secret", "tuple" to listOf("a", "b", "c", "tuple-secret")))
      )
      .withSecretsPolicy(SecretTypeSecretsPolicy)
      .withReport()
      .withReportPrintFn { report.append(it) }
      .build()
      .loadConfigOrThrow<Test>()

    report.toString() shouldNotContain "option-secret"
    report.toString() shouldNotContain "tuple-secret"
  }

  test("a policy on the option type still sees it") {
    data class Test(val option: Option<String>, val other: String)

    val optionType = Option::class.createType(listOf(KTypeProjection.invariant(String::class.createType())))
    val policy = object : SecretsPolicy {
      override fun isSecret(node: Node, type: KType?): Boolean = type == optionType
    }
    val report = StringBuilder()
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.map(mapOf("option" to "option-secret", "other" to "plain-value")))
      .withSecretsPolicy(policy)
      .withReport()
      .withReportPrintFn { report.append(it) }
      .build()
      .loadConfigOrThrow<Test>()

    report.toString() shouldNotContain "option-secret"
    report.toString() shouldContain "plain-value"
  }
})
