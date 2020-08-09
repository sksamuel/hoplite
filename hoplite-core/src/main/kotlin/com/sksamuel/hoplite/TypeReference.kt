package com.sksamuel.hoplite

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType

abstract class TypeReference<A : Any> {

  companion object {
    inline fun <reified A : Any> create(): TypeReference<A> = object : TypeReference<A>() {}
  }

  // the kind of A
  // will be a Class if A is a proper type
  // will be paramterizedType if A is a higher order type
  private val type: Type

  fun createKType(): KType {
    return when (type) {
      is Class<*> -> type.kotlin.createType()
      is ParameterizedType -> {

        val clazz = type.rawType as? Class<*>
          ?: throw java.lang.IllegalArgumentException("Unsupported top level type ${type.rawType}")

        val listType = when (val typeArg = type.actualTypeArguments[0]) {
          is Class<*> -> typeArg as Class<Any>
          is WildcardType -> typeArg.upperBounds[0] as Class<Any>
          else -> throw IllegalArgumentException("Unsupported actualTypeArgument $typeArg")
        }

        clazz.kotlin.createType(listOf(KTypeProjection(KVariance.OUT, listType.kotlin.createType())))
      }
      else -> throw IllegalArgumentException("Unsupported kind $type")
    }
  }

  init {
    val superClass = this::class.java.genericSuperclass
    // should always be a parameterized type
    type = when (superClass) {
      // the actual type argument is the concrete type that was passed into this type constructor
      is ParameterizedType -> superClass.actualTypeArguments[0]
      else -> throw IllegalArgumentException("Type was constructed without type parameter information")
    }
  }
}
