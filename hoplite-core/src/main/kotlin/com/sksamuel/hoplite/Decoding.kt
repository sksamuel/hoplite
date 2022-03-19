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

  fun <A : Any> decode(kclass: KClass<A>, node: Node, mode: DecodeMode): ConfigResult<DecodingResult<A>> {
    val context = DecoderContext(decoderRegistry, paramMappers, preprocessors, mutableSetOf())
    val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
    val errors = UnresolvedSubstitutionChecker.process(preprocessed)
    return if (errors.isNotEmpty())
      ConfigFailure.MultipleFailures(NonEmptyList(errors)).invalid()
    else {
      decoderRegistry.decoder(kclass)
        .flatMap { it.decode(preprocessed, kclass.createType(), context) }
        .flatMap { decodingResult(it, node, context.usedPaths, mode) }
    }
  }

  private fun <A : Any> decodingResult(
    a: A,
    node: Node,
    usedPaths: MutableSet<DotPath>,
    mode: DecodeMode,
  ): ConfigResult<DecodingResult<A>> {
    val (used, unused) = node.paths().filterNot { it.first == DotPath.root }.partition { usedPaths.contains(it.first) }
    val result = DecodingResult(a, used, unused)
    return when (mode) {
      DecodeMode.Strict -> ensureAllUsed(result)
      DecodeMode.Lenient -> result.valid()
    }
  }

  private fun <A : Any> ensureAllUsed(result: DecodingResult<A>): ConfigResult<DecodingResult<A>> {
    return if (result.unused.isEmpty()) result.valid() else {
      val errors = result.unused.map { ConfigFailure.UnusedPaths(it.first, it.second) }.nel()
      ConfigFailure.MultipleFailures(errors).invalid()
    }
  }
}

data class DecodingResult<A>(val a: A, val used: List<Pair<DotPath, Pos>>, val unused: List<Pair<DotPath, Pos>>)
