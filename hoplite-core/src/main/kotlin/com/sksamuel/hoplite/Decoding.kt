package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
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
    return context.preprocessors.fold<Preprocessor, ConfigResult<Node>>(node.valid()) { acc, preprocessor ->
      acc.flatMap { preprocessor.process(it) }
    }.flatMap {
      UnresolvedSubstitutionChecker.process(it)
    }.flatMap { preprocessed ->
      decoderRegistry.decoder(kclass)
        .flatMap { it.decode(preprocessed, kclass.createType(), context) }
        .flatMap { decodingResult(it, preprocessed, context.usedPaths, mode, context.secrets) }
    }
  }

  private fun <A : Any> decodingResult(
    a: A,
    node: Node,
    usedPaths: MutableSet<DotPath>,
    mode: DecodeMode,
    secrets: MutableSet<DotPath>,
  ): ConfigResult<DecodingResult<A>> {
    val (used, unused) = node.paths().filterNot { it.first == DotPath.root }.partition { usedPaths.contains(it.first) }
    val result = DecodingResult(a, used, unused, secrets.toSet())
    return when (mode) {
      DecodeMode.Strict -> ensureAllUsed(result)
      DecodeMode.Lenient -> result.valid()
    }
  }

  private fun <A : Any> ensureAllUsed(result: DecodingResult<A>): ConfigResult<DecodingResult<A>> {
    return if (result.unused.isEmpty()) result.valid() else {
      val errors = NonEmptyList.unsafe(result.unused.map { ConfigFailure.UnusedPath(it.first, it.second) })
      ConfigFailure.MultipleFailures(errors).invalid()
    }
  }
}

data class DecodingResult<A>(
  val a: A,
  val used: List<Pair<DotPath, Pos>>,
  val unused: List<Pair<DotPath, Pos>>,
  val secrets: Set<DotPath>,
)
