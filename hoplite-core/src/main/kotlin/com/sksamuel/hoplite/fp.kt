package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated

// just a validated instance where the error type is fixed to ConfigFailure
typealias ConfigResult<A> = Validated<ConfigFailure, A>

fun <T, U> Result<T>.flatMap(f: (T) -> Result<U>) = fold({ f(it) }, { Result.failure<Nothing>(it) })

internal fun <T> List<Result<T>>.sequence(): Result<List<T>> {
  val ss = this.map { result -> result.getOrElse { return Result.failure(it) } }
  return Result.success(ss)
}

fun <A, E> Result<A>.toValidated(ifFailure: (Throwable) -> E): Validated<E, A> =
  fold({ Validated.Valid(it) }, { Validated.Invalid(ifFailure(it)) })
