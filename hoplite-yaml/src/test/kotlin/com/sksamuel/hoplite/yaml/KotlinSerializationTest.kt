package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ParameterMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties

class KotlinSerializationTest : FunSpec() {
  init {
    test("parameter mappers should have access to annotations on properties") {
      ConfigLoaderBuilder.default()
        .addParameterMapper(SerialNameMapper)
        .addSource(YamlPropertySource("my-property: foo"))
        .build()
        .loadConfigOrThrow<Config>()
        .myProperty shouldBe "foo"
    }
  }
}

@Serializable
data class Config(
  @SerialName("my-property")
  val myProperty: String,
)

object SerialNameMapper : ParameterMapper {

  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val annotation =
      kclass.declaredMemberProperties.find { it.name == param.name }
        ?.annotations
        ?.filterIsInstance<SerialName>()
        ?.firstOrNull()
        ?: return emptySet()
    return setOf(annotation.value)
  }
}
