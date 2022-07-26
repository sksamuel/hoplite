package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.sksamuel.hoplite.Secret

object HopliteModule : SimpleModule() {
  init {
    addSerializer(Secret::class.javaObjectType, object : JsonSerializer<Secret>() {
      override fun serialize(value: Secret, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString("****")
      }
    })
  }
}
