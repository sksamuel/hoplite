package com.sksamuel.hoplite.arrow

import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated

fun <E, A, B> Validated<E, A>.flatMap(f: (A) -> Validated<E, B>): Validated<E, B> {
  return when (this) {
    is Invalid -> this
    is Valid -> f(this.a)
  }
}