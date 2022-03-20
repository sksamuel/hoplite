package com.sksamuel.hoplite

typealias Masked = Secret

data class Secret(val value: String) {
  override fun toString(): String = "****"
}
