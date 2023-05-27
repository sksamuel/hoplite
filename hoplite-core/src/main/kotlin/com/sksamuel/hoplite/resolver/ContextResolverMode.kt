package com.sksamuel.hoplite.resolver

enum class ContextResolverMode {

  // do not fail if the substitution path is not found
  SkipUnresolved,

  // fail if the substitution path is not found
  ErrorOnUnresolved,
}
