package com.sksamuel.hoplite.fp

sealed class Try<out A> {

  data class Success<T>(val value: T) : Try<T>()
  data class Failure(val throwable: Throwable) : Try<Nothing>()

  companion object {
    inline operator fun <T> invoke(f: () -> T): Try<T> = try {
      Success(f())
    } catch (t: Throwable) {
      Failure(t)
    }
  }

  inline fun <U> fold(ifFailure: (Throwable) -> U, ifSuccess: (A) -> U): U = when (this) {
    is Success -> ifSuccess(this.value)
    is Failure -> ifFailure(throwable)
  }

  inline fun onFailure(f: (Throwable) -> Unit): Try<A> {
    fold({ f(it) }, {})
    return this
  }

  fun orNull(): A? = when (this) {
    is Success -> this.value
    is Failure -> null
  }

  fun <E> toValidated(ifFailure: (Throwable) -> E): Validated<E, A> = when (this) {
    is Success -> this.value.valid()
    is Failure -> ifFailure(this.throwable).invalid()
  }

  inline fun <B> map(f: (A) -> B): Try<B> = when (this) {
    is Success -> Success(f(this.value))
    is Failure -> this
  }

  inline fun <B> flatMap(f: (A) -> Try<B>): Try<B> = when (this) {
    is Success -> f(this.value)
    is Failure -> this
  }
}

inline fun <A> Try<A>.getOrElse(ifFailure: (Throwable) -> A): A = fold({ ifFailure(it) }, { it })

