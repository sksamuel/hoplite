package com.sksamuel.hoplite.arrow

import arrow.core.Failure
import arrow.core.Success
import arrow.core.Try
import arrow.data.Validated
import arrow.data.invalid
import arrow.data.valid

fun <E, A> Try<A>.toValidated(ifFailure: (Throwable) -> E): Validated<E, A> = when (this) {
  is Success -> this.value.valid()
  is Failure -> ifFailure(this.exception).invalid()
}