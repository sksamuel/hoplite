package com.sksamuel.hoplite.resolver.context

enum class ContextResolverMode {

  // do not fail if the substitution path is not found, leaving the expression intact.
  SkipUnresolved,

  // fail if the substitution path is not found
  ErrorOnUnresolved,
}
