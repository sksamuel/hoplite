package com.sksamuel.hoplite.arrow

import arrow.core.Invalid
import arrow.core.NonEmptyList
import arrow.core.Tuple2
import arrow.core.Valid
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.fix
import arrow.core.extensions.list.traverse.sequence

fun <E, A> List<Validated<E, A>>.sequence(): ValidatedNel<E, List<A>> =
  this.map { it.toValidatedNel() }
    .sequence(Validated.applicative(NonEmptyList.semigroup<E>())).fix().map { a -> a.fix().toList() }

fun <E, A1, A2, B> ap(v1: Validated<E, A1>,
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
