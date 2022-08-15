package com.sksamuel.hoplite.env

data class Environment(val name: String) {

  companion object {

    val prod = Environment("prod")
    val production = Environment("production")
    val dev = Environment("dev")
    val development = Environment("development")
    val qa = Environment("qa")
    val staging = Environment("staging")
    val local = Environment("local")
    val test = Environment("test")

    fun fromEnvVar(envVarName: String): Environment {
      val env = System.getenv(envVarName)
      if (env.isNullOrBlank()) error("Environment variable $envVarName must be specified")
      return Environment(env)
    }
  }
}
