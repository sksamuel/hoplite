package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.flatMap
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class InlineClassDecoder : NullHandlingDecoder<Any> {

  override fun supports(type: KType): Boolean = when (val classifer = type.classifier) {
    is KClass<*> -> classifer.isInline()
    else -> false
  }

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Any> {

    val constructor = (type.classifier as KClass<*>).primaryConstructor?.valid()
      ?: ConfigFailure.MissingPrimaryConstructor(type).invalid()

    return constructor.flatMap { constr ->

      val param = constr.parameters[0]

      if (node is Undefined) {
        ConfigFailure.NullValueForNonNullField(node).invalid()
      } else {
        context.decoder(param)
          .flatMap { it.decode(node, param.type, context) }
          .mapInvalid { ConfigFailure.IncompatibleInlineType(param.type, node) }
          .flatMap { construct(type, constr, mapOf(param to it)) }
      }
    }
  }

  private fun <A> construct(
    type: KType,
    constructor: KFunction<A>,
    args: Map<KParameter, Any?>
  ): ConfigResult<A> {
    return try {
      constructor.callBy(args).valid()
    } catch (e: InvocationTargetException) {
      ConfigFailure.InvalidConstructorParameters(type, constructor, args, e.cause ?: e).invalid()
    } catch (e: IllegalArgumentException) {
      ConfigFailure.InvalidConstructorParameters(type, constructor, args, e.cause ?: e).invalid()
    }
  }
}

/**
 * True for a value class with one property. Answered from the class file rather than kotlin-reflect, as this is
 * asked of every type a decoder is looked up for: only a value class has a `box-impl` function, a value class is
 * never a data class, and its `constructor-impl` function takes the primary constructor's parameters.
 */
fun KClass<*>.isInline(): Boolean {
  val methods = java.declaredMethods
  return methods.any { it.name == "box-impl" } &&
    methods.any { it.name == "constructor-impl" && it.parameterCount == 1 }
}
