package com.sksamuel.hoplite.env

data class Environment(val name: String) {
  companion object {
    fun fromEnvVar(name: String): Environment {
      val env = System.getenv(name)
      if (env.isNullOrBlank()) error("Environment variable $name must be specified")
      return Environment(env)
    }
  }
}
