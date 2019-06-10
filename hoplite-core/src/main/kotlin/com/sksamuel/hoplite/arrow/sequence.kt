package com.sksamuel.hoplite.arrow

import arrow.data.NonEmptyList
import arrow.data.Validated
import arrow.data.ValidatedNel
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.extensions.validated.applicative.applicative
import arrow.data.fix

fun <E, A> List<ValidatedNel<E, A>>.sequence(): ValidatedNel<E, List<A>> =
    this.sequence(Validated.applicative(NonEmptyList.semigroup<E>())).fix().map { a -> a.fix().toList() }