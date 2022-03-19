package com.sksamuel.hoplite

data class Secret(val value: String) {
  override fun toString(): String = value.take(2) + "****"
}

data class Masked(val value: String) {
  override fun toString(): String = "****"
}
