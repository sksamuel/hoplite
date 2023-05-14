package com.sksamuel.hoplite.resolver

enum class ContextResolverMode {

  // do not fail if the substitution path is not found
  Silent,

  // fail if the substitution path is not found
  Error,
}
