package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.nel
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.UnresolvedSubstitutionChecker
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class Decoding(
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val preprocessors: List<Preprocessor>,
) {

  fun <A : Any> decode(kclass: KClass<A>, node: Node, mode: DecodeMode): ConfigResult<A> {
    val context = DecoderContext(decoderRegistry, paramMappers, preprocessors, mutableSetOf())
    val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
    val errors = UnresolvedSubstitutionChecker.process(preprocessed)
    return if (errors.isNotEmpty())
      ConfigFailure.MultipleFailures(NonEmptyList(errors)).invalid()
    else
      decoderRegistry.decoder(kclass)
        .flatMap { it.decode(preprocessed, kclass.createType(), context) }
        .flatMap { if (mode == DecodeMode.Strict) ensureAllUsed(it, node, context.usedPaths) else it.valid() }
  }

  private fun <A : Any> ensureAllUsed(a: A, node: Node, usedPaths: Set<DotPath>): ConfigResult<A> {
    val unused = node.paths().filterNot { usedPaths.contains(it.first) }.filterNot { it.first == DotPath.root }
    return if (unused.isEmpty()) a.valid() else {
      val errors = unused.map { ConfigFailure.UnusedPaths(it.first, it.second) }.nel()
      ConfigFailure.MultipleFailures(errors).invalid()
    }
  }
}
