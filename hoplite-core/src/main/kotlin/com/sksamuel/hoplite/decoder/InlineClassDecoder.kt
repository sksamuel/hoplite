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

fun KClass<*>.isInline(): Boolean {
  return !isData &&
    primaryConstructor?.parameters?.size == 1 &&
    java.declaredMethods.any { it.name == "box-impl" }
}
