package com.sksamuel.hoplite.env

data class Environment(val name: String) {

  companion object {

    // some commonly defined environments

    val prod = Environment("prod")
    val production = Environment("production")
    val dev = Environment("dev")
    val development = Environment("development")
    val qa = Environment("qa")
    val staging = Environment("staging")
    val local = Environment("local")
    val live = Environment("live")
    val test = Environment("test")

    /**
     * Returns an [Environment] created by looking up the env-var with the given [name].
     * If the env-var does not exist, an error is thrown.
     */
    fun fromEnvVar(envVarName: String): Environment {
      val env = System.getenv(envVarName)
      if (env.isNullOrBlank()) error("Environment variable $envVarName must be specified")
      return Environment(env)
    }
  }
}
