package com.sksamuel.hoplite.resolver.context

enum class ContextResolverMode {

  // do not fail if the substitution path is not found
  SkipUnresolved,

  // fail if the substitution path is not found
  ErrorOnUnresolved,
}
