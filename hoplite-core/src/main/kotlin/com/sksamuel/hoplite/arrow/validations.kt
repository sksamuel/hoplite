package com.sksamuel.hoplite.arrow

import arrow.core.Tuple2
import arrow.data.Invalid
import arrow.data.NonEmptyList
import arrow.data.Valid
import arrow.data.Validated
import arrow.data.ValidatedNel
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.extensions.validated.applicative.applicative
import arrow.data.fix
import arrow.data.valid

fun <E, A> List<Validated<E, A>>.sequence(): ValidatedNel<E, List<A>> =
  this.map { it.toValidatedNel() }
    .sequence(Validated.applicative(NonEmptyList.semigroup<E>())).fix().map { a -> a.fix().toList() }

fun <E, A1, A2, B> ap(v1: Validated<E, A1>, v2: Validated<E, A2>, f: (Tuple2<A1, A2>) -> B): Validated<E, B> {
  return when (v1) {
    is Valid -> when (v2) {
      is Valid -> f(Tuple2(v1.a, v2.a)).valid()
      is Invalid -> v2
    }
    is Invalid -> v1
  }
}

@JvmName("flatApply")
fun <E, A1, A2, B> app(v1: Validated<E, A1>,
                       v2: Validated<E, A2>,
                       f: (Tuple2<A1, A2>) -> Validated<E, B>): Validated<E, B> {
  return when (v1) {
    is Valid -> when (v2) {
      is Valid -> f(Tuple2(v1.a, v2.a))
      is Invalid -> v2
    }
    is Invalid -> v1
  }
}
