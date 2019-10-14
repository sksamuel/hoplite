package com.sksamuel.hoplite.arrow

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.Validated

fun <E, A, B> Validated<E, A>.flatMap(f: (A) -> Validated<E, B>): Validated<E, B> {
  return when (this) {
    is Invalid -> this
    is Valid -> f(this.a)
  }
}
