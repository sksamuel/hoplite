package com.sksamuel.hoplite.fp.result

import com.sksamuel.hoplite.fp.Validated

fun <T, U> Result<T>.flatMap(f: (T) -> Result<U>) = fold({ f(it) }, { Result.failure<Nothing>(it) })

fun <T> List<Result<T>>.sequence(): Result<List<T>> {
  val ss = this.map { result -> result.getOrElse { return Result.failure(it) } }
  return Result.success(ss)
}

fun <A, E> Result<A>.toValidated(f: (Throwable) -> E): Validated<E, A> =
  fold({ Validated.Valid(it) }, { Validated.Invalid(f(it)) })
