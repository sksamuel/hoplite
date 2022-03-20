package com.sksamuel.hoplite.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.hoplite.ParameterMapper
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

object JsonPropertyParamMapper : ParameterMapper {
  override fun map(param: KParameter): Set<String> {
    val jsonProperty = param.findAnnotation<JsonProperty>()
    val value = jsonProperty?.value ?: param.name
    return setOfNotNull(value)
  }
}
