package com.sksamuel.hoplite.env

/**
 * Specifies the [Environment] that config is being loaded into. For example, prod, staging or local.
 *
 * This class can be used to restrict settings to particular environments. For example, you may wish
 * to apply different pre-processors in prod than staging, or enable a property source
 * only in local or test.
 */
data class Environment(val name: String) {

  override fun toString(): String = name

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

    /**
     * Returns an [Environment] created by looking up the env-var with the given [name].
     * If the env-var does not exist, the fallback value is used
     */
    fun fromEnvVar(envVarName: String, fallback: Environment): Environment {
      val env = System.getenv(envVarName)
      return if (env.isNullOrBlank()) fallback else Environment(env)
    }
  }
}
