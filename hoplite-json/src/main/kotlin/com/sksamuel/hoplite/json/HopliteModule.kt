package com.sksamuel.hoplite.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.sksamuel.hoplite.Masked

object HopliteModule : SimpleModule() {
  init {
    this.addSerializer(Masked::class.javaObjectType, object : JsonSerializer<Masked>() {
      override fun serialize(value: Masked, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString("****")
      }
    })
  }
}
