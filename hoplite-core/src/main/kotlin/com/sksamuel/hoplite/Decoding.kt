package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.UnresolvedSubstitutionChecker
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class Decoding(
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val preprocessors: List<Preprocessor>
) {

  fun <A : Any> decode(kclass: KClass<A>, node: Node): ConfigResult<A> {
    val context = DecoderContext(decoderRegistry, paramMappers, preprocessors, mutableSetOf(), DecodeMode.Lenient)
    val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
    val errors = UnresolvedSubstitutionChecker.process(preprocessed)
    return if (errors.isNotEmpty())
      ConfigFailure.MultipleFailures(NonEmptyList(errors)).invalid()
    else
      decoderRegistry.decoder(kclass).flatMap { it.decode(preprocessed, kclass.createType(), context) }
  }
}
