package com.sksamuel.hoplite.fp

sealed class Validated<out E, out A> {

  data class Invalid<E>(val error: E) : Validated<E, Nothing>()
  data class Valid<A>(val value: A) : Validated<Nothing, A>()

  fun isValid() = this is Valid<*>
  fun isInvalid() = this is Invalid<*>

  fun getUnsafe(): A = when (this) {
    is Invalid -> throw IllegalStateException("Not a valid instance, was $this")
    is Valid -> this.value
  }

  fun getInvalidUnsafe(): E = when (this) {
    is Invalid -> this.error
    is Valid -> throw IllegalStateException("Not an invalid instance, was $this")
  }

  fun <B> map(f: (A) -> B): Validated<E, B> = when (this) {
    is Valid -> f(this.value).valid()
    is Invalid -> this
  }

  fun onSuccess(f: (A) -> Unit): Validated<E, A> {
    when (this) {
      is Valid -> f(this.value)
      is Invalid -> Unit
    }
    return this
  }

  fun onFailure(f: (E) -> Unit): Validated<E, A> {
    when (this) {
      is Valid -> Unit
      is Invalid -> f(this.error)
    }
    return this
  }

  fun <F> mapInvalid(f: (E) -> F): Validated<F, A> = when (this) {
    is Invalid -> f(this.error).invalid()
    is Valid -> this
  }

  fun toValidatedNel(): Validated<NonEmptyList<E>, A> = when (this) {
    is Valid -> value.valid()
    is Invalid -> NonEmptyList.of(error).invalid()
  }

  fun <T> fold(ifInvalid: (E) -> T, ifValid: (A) -> T): T {
    return when (this) {
      is Valid -> ifValid(this.value)
      is Invalid -> ifInvalid(this.error)
    }
  }

  companion object {

    fun <A, B, R, E> ap(a: Validated<E, A>,
                        b: Validated<E, B>,
                        f: (A, B) -> R): Validated<NonEmptyList<E>, R> {
      return when (a) {
        is Invalid -> when (b) {
          is Invalid -> NonEmptyList.of(a.error, b.error).invalid()
          is Valid -> NonEmptyList.of(a.error).invalid()
        }
        is Valid -> when (b) {
          is Invalid -> NonEmptyList.of(b.error).invalid()
          is Valid -> f(a.value, b.value).valid()
        }
      }
    }

    fun <A, B, C, R, E> ap(a: Validated<E, A>,
                           b: Validated<E, B>,
                           c: Validated<E, C>,
                           f: (A, B, C) -> R): Validated<NonEmptyList<E>, R> {
      val errors = listOf(a, b, c).filterIsInstance<Invalid<E>>().map { it.error }
      return if (errors.isNotEmpty()) NonEmptyList(errors).invalid() else {
        f(a.getUnsafe(), b.getUnsafe(), c.getUnsafe()).valid()
      }
    }

    fun <A, B, C, D, E, R> ap(a: Validated<E, A>,
                              b: Validated<E, B>,
                              c: Validated<E, C>,
                              d: Validated<E, D>,
                              f: (A, B, C, D) -> R): Validated<NonEmptyList<E>, R> {
      val errors = listOf(a, b, c, d).filterIsInstance<Invalid<E>>().map { it.error }
      return if (errors.isNotEmpty()) NonEmptyList(errors).invalid() else {
        f(a.getUnsafe(), b.getUnsafe(), c.getUnsafe(), d.getUnsafe()).valid()
      }
    }

    fun <A, B, C, D, E, Z, R> ap(a: Validated<Z, A>,
                                 b: Validated<Z, B>,
                                 c: Validated<Z, C>,
                                 d: Validated<Z, D>,
                                 e: Validated<Z, E>,
                                 f: (A, B, C, D, E) -> R): Validated<NonEmptyList<Z>, R> {
      val errors = listOf(a, b, c, d, e).filterIsInstance<Invalid<Z>>().map { it.error }
      return if (errors.isNotEmpty()) NonEmptyList(errors).invalid() else {
        f(a.getUnsafe(), b.getUnsafe(), c.getUnsafe(), d.getUnsafe(), e.getUnsafe()).valid()
      }
    }
  }
}

typealias ValidatedNel<E, A> = Validated<NonEmptyList<E>, A>

fun <E, A, B : A> Validated<E, A>.getOrElse(ifInvalid: (E) -> B): A = when (this) {
  is Validated.Valid -> this.value
  is Validated.Invalid -> ifInvalid(this.error)
}

fun <A> A.valid(): Validated<Nothing, A> = Validated.Valid(this)
fun <E> E.invalid(): Validated<E, Nothing> = Validated.Invalid(this)

fun <E, F, A> Validated<E, A>.flatRecover(f: (E) -> Validated<F, A>): Validated<F, A> = when (this) {
  is Validated.Invalid -> f(this.error)
  is Validated.Valid -> this
}

inline fun <E, A, B> Validated<E, A>.flatMap(f: (A) -> Validated<E, B>) = when (this) {
  is Validated.Invalid -> this
  is Validated.Valid -> f(this.value)
}

fun <A, E> List<Validated<E, A>>.sequence(): Validated<NonEmptyList<E>, List<A>> {
  val invalids = filterIsInstance<Validated.Invalid<E>>()
  val valids = filterIsInstance<Validated.Valid<A>>()
  return if (invalids.isEmpty())
    valids.map { it.value }.valid()
  else
    NonEmptyList.unsafe(invalids.map { it.error }).invalid()
}
