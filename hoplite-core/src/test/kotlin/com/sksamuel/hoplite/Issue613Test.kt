package com.sksamuel.hoplite

import com.sksamuel.hoplite.secrets.SecretTypeSecretsPolicy
import com.sksamuel.hoplite.secrets.SecretsPolicy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

data class Issue613Api(
  val url: String,
  val token: Secret,
  val headers: Map<String, Secret>,
  val tokens: List<Secret>,
)

data class Issue613Config(val api: Issue613Api)

data class Issue613Collections(
  val linked: LinkedHashMap<String, Secret>,
  val set: Set<Secret>,
  val csv: List<Secret>,
  val csvSet: Set<Secret>,
  val nested: Map<String, List<Secret>>,
  val strings: Map<String, String>,
)

data class Issue613ArrayMap(val headers: Map<String, Secret>)

data class Issue613Token(val token: Secret)
data class Issue613DataClasses(val list: List<Issue613Token>, val map: Map<String, Issue613Token>)

sealed interface Issue613Auth
data class Issue613AuthSecrets(val tokens: List<Secret>) : Issue613Auth
data class Issue613AuthString(val tokens: String) : Issue613Auth
data class Issue613Sealed(val auth: Issue613Auth)

data class Issue613Pair(val pair: Pair<String, Secret>)
data class Issue613Triple(val triple: Triple<Secret, String, String>)
data class Issue613Strings(val csv: List<String>, val array: List<String>, val other: String)

data class Issue613Constructors(val token: String) {
  constructor(token: Secret, suffix: String = "!") : this(token.value + suffix)
}

data class Issue613Nullable(val token: Secret?, val headers: Map<String, Secret?>)

@OptIn(ExperimentalHoplite::class)
class Issue613Test : FunSpec({

  // gh-613: the collection decoders did not record the type of the elements they decoded, so map entries were
  // reported as unused, and type based secrets policies did not mask Map<_, Secret> or List<Secret> values.
  fun report(source: PropertySource, policy: SecretsPolicy, load: ConfigLoader.() -> Unit): String {
    val out = StringBuilder()
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(source)
      .withSecretsPolicy(policy)
      .withReport()
      .withReportPrintFn { out.append(it) }
      .strict()
      .build()
      .load()
    return out.toString()
  }

  test("map and list of secrets are masked and map entries are not unused") {
    val source = PropertySource.map(
      mapOf(
        "api" to mapOf(
          "url" to "https://api.example",
          "token" to "token-secret",
          "headers" to mapOf("auth" to "header-secret"),
          "tokens" to listOf("list-secret-1", "list-secret-2"),
        )
      )
    )
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613Config>().api.headers["auth"]?.value shouldBe "header-secret"
    }
    out shouldContain "Unused keys: none"
    out shouldContain "hea*****"
    out shouldNotContain "header-secret"
    out shouldNotContain "list-secret"
    out shouldContain "https://api.example"
  }

  test("linked hash map, set, comma separated list and nested collections of secrets are masked") {
    val source = PropertySource.map(
      mapOf(
        "linked" to mapOf("a" to "linked-secret"),
        "set" to listOf("set-secret-1", "set-secret-2"),
        "csv" to "csv-secret-1,csv-secret-2",
        "csvSet" to "csvset-secret-1,csvset-secret-2",
        "nested" to mapOf("a" to listOf("nested-secret-1", "nested-secret-2")),
        "strings" to mapOf("b" to "plain-value"),
      )
    )
    val out = report(source, SecretTypeSecretsPolicy) { loadConfigOrThrow<Issue613Collections>() }
    out shouldContain "Unused keys: none"
    out shouldNotContain "linked-secret"
    out shouldNotContain "set-secret"
    out shouldNotContain "csv-secret"
    out shouldNotContain "csvset-secret"
    out shouldNotContain "nested-secret"
    out shouldContain "plain-value"
  }

  test("array form of a map is not unused and its secret values are masked") {
    val source = PropertySource.map(
      mapOf("headers" to listOf(mapOf("key" to "auth", "value" to "header-secret")))
    )
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613ArrayMap>().headers["auth"]?.value shouldBe "header-secret"
    }
    out shouldContain "Unused keys: none"
    out shouldNotContain "header-secret"
    out shouldContain "auth"
  }

  test("collections of data classes are still reported with the field types") {
    val source = PropertySource.map(
      mapOf(
        "list" to listOf(mapOf("token" to "list-secret")),
        "map" to mapOf("a" to mapOf("token" to "map-secret")),
      )
    )
    val out = report(source, SecretTypeSecretsPolicy) { loadConfigOrThrow<Issue613DataClasses>() }
    out shouldContain "Unused keys: none"
    out shouldNotContain "list-secret"
    out shouldNotContain "map-secret"
  }

  test("the report uses the types of the sealed subtype that was picked") {
    val source = PropertySource.map(mapOf("auth" to mapOf("tokens" to "sealed-secret-1,sealed-secret-2")))
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613Sealed>().auth::class shouldBe Issue613AuthSecrets::class
    }
    out shouldNotContain "sealed-secret"
  }

  test("the report uses the types of the constructor that was picked") {
    val source = PropertySource.map(mapOf("token" to "ctor-secret"))
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613Constructors>().token shouldBe "ctor-secret!"
    }
    out shouldNotContain "ctor-secret"
  }

  test("comma separated triple with a secret element is masked") {
    val source = PropertySource.map(mapOf("triple" to "triple-secret,a,b"))
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613Triple>().triple.first.value shouldBe "triple-secret"
    }
    out shouldNotContain "triple-secret"
  }

  test("a policy on the collection type still sees it") {
    val listType = List::class.createType(listOf(KTypeProjection.invariant(String::class.createType())))
    val policy = object : SecretsPolicy {
      override fun isSecret(node: Node, type: KType?): Boolean = type == listType
    }
    val source = PropertySource.map(
      mapOf("csv" to "csv-secret-1,csv-secret-2", "array" to listOf("array-secret"), "other" to "plain-value")
    )
    val out = report(source, policy) { loadConfigOrThrow<Issue613Strings>() }
    out shouldNotContain "csv-secret"
    out shouldNotContain "array-secret"
    out shouldContain "plain-value"
  }

  test("pair with a secret element is masked") {
    val source = PropertySource.map(mapOf("pair" to listOf("pair-plain", "pair-secret")))
    val out = report(source, SecretTypeSecretsPolicy) {
      loadConfigOrThrow<Issue613Pair>().pair.second.value shouldBe "pair-secret"
    }
    out shouldContain "Unused keys: none"
    out shouldNotContain "pair-secret"
  }

  test("nullable secrets are masked") {
    val source = PropertySource.map(
      mapOf("token" to "nullable-secret", "headers" to mapOf("a" to "nullable-map-secret"))
    )
    val out = report(source, SecretTypeSecretsPolicy) { loadConfigOrThrow<Issue613Nullable>() }
    out shouldContain "Unused keys: none"
    out shouldNotContain "nullable-secret"
    out shouldNotContain "nullable-map-secret"
  }
})
