package com.sksamuel.hoplite.fp

class NonEmptyList<out A>(val list: List<A>) {

  init {
    require(list.isNotEmpty())
  }

  fun <B> map(f: (A) -> B): NonEmptyList<B> {
    return NonEmptyList(list.map(f))
  }

  companion object {

    fun <A> unsafe(list: List<A>) = NonEmptyList(list)

    fun <A> of(a: A, vararg more: A): NonEmptyList<A> {
      return NonEmptyList(listOf(a) + more)
    }
  }
}
