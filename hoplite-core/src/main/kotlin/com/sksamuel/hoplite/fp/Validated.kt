package com.sksamuel.hoplite.fp

sealed class Validated<out E, out A> {

  data class Invalid<E>(val error: E) : Validated<E, Nothing>()
  data class Valid<A>(val value: A) : Validated<Nothing, A>()

  fun isInvalid() = this is Invalid<*>

  fun getUnsafe(): A = when (this) {
    is Invalid -> throw IllegalStateException("Not a valid instance, was $this")
    is Valid -> this.value
  }

  fun <B> map(f: (A) -> B): Validated<E, B> = when (this) {
    is Valid -> f(this.value).valid()
    is Invalid -> this
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
        is Validated.Invalid -> when (b) {
          is Validated.Invalid -> NonEmptyList.of(a.error, b.error).invalid()
          is Validated.Valid -> NonEmptyList.of(a.error).invalid()
        }
        is Validated.Valid -> when (b) {
          is Validated.Invalid -> NonEmptyList.of(b.error).invalid()
          is Validated.Valid -> f(a.value, b.value).valid()
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

inline fun <E, A, B> Validated<E, A>.map(f: (A) -> B) = when (this) {
  is Validated.Invalid -> this
  is Validated.Valid -> f(this.value).valid()
}

fun <E, F, A> Validated<E, A>.flatMapInvalid(f: (E) -> Validated<F, A>): Validated<F, A> = when (this) {
  is Validated.Invalid -> f(this.error)
  is Validated.Valid -> this
}

inline fun <E, A, B> Validated<E, A>.flatMap(f: (A) -> Validated<E, B>) = when (this) {
  is Validated.Invalid -> this
  is Validated.Valid -> f(this.value)
}

inline fun <A, E, T> Validated<E, A>.fold(ifInvalid: (E) -> T, ifValid: (A) -> T): T = when (this) {
  is Validated.Invalid -> ifInvalid(error)
  is Validated.Valid -> ifValid(value)
}

inline fun <A, E> Validated<E, A>.onValid(f: (A) -> Unit): Validated<E, A> = when (this) {
  is Validated.Invalid -> this
  is Validated.Valid -> {
    f(this.value)
    this
  }
}

inline fun <A, E> Validated<E, A>.onInvalid(f: (E) -> Unit): Validated<E, A> = when (this) {
  is Validated.Valid -> this
  is Validated.Invalid -> {
    f(this.error)
    this
  }
}


inline fun <A, E, F> Validated<E, A>.mapInvalid(f: (E) -> F): Validated<F, A> = when (this) {
  is Validated.Invalid -> f(error).invalid()
  is Validated.Valid -> this
}


fun <A, E> List<Validated<E, A>>.sequence(): Validated<NonEmptyList<E>, List<A>> {
  val invalids = filterIsInstance<Validated.Invalid<E>>()
  val valids = filterIsInstance<Validated.Valid<A>>()
  return if (invalids.isEmpty()) valids.map { it.value }.valid() else invalids.map { it.error }.nel().invalid()
}
