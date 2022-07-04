package com.sksamuel.hoplite.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.hoplite.ParameterMapper
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

object JsonPropertyParamMapper : ParameterMapper {
  override fun map(param: KParameter, constructor: KFunction<Any>, kclass: KClass<*>): Set<String> {
    val jsonProperty = param.findAnnotation<JsonProperty>()
    val value = jsonProperty?.value ?: param.name
    return setOfNotNull(value)
  }
}
