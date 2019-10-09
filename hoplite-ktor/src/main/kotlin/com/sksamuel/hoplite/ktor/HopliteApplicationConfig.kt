package com.sksamuel.hoplite.ktor

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.PrimitiveValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.UndefinedValue
import com.sksamuel.hoplite.hasKeyAt
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.LoggerFactory
import java.nio.file.Path

@KtorExperimentalAPI
class HopliteApplicationConfig(private val value: Value) : ApplicationConfig {

  override fun config(path: String): ApplicationConfig = HopliteApplicationConfig(value.atKey(path))

  override fun configList(path: String): List<ApplicationConfig> = emptyList()

  override fun property(path: String): ApplicationConfigValue = HopliteApplicationConfigValue(value.atKey(path))

  override fun propertyOrNull(path: String): ApplicationConfigValue? =
    if (value.hasKeyAt(path)) property(path) else null
}

@KtorExperimentalAPI
class HopliteApplicationConfigValue(private val value: Value) : ApplicationConfigValue {

  override fun getString(): String = when (value) {
    is PrimitiveValue -> value.value.toString()
    else -> throw IllegalArgumentException("${value.simpleName} cannot be converted to string")
  }

  override fun getList(): List<String> = when (value) {
    is ListValue -> value.elements.map { element ->
      when (element) {
        is PrimitiveValue -> element.value.toString()
        else -> throw IllegalArgumentException("${element.simpleName} cannot be converted to string")
      }
    }
    is StringValue -> value.value.split(',').toList()
    else -> throw IllegalArgumentException("${value.simpleName} cannot be converted to list")
  }
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationEngineEnvironment(first: String, vararg tail: String): ApplicationEngineEnvironment =
  loadApplicationEngineEnvironment(listOf(first) + tail)

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationEngineEnvironment(resources: List<String>): ApplicationEngineEnvironment {
  val node = loadNodeOrThrow(resources)
  return hopliteApplicationEngineEnvironment(node)
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationEngineEnvironment(first: Path, vararg tail: Path): ApplicationEngineEnvironment =
  loadApplicationEngineEnvironment(listOf(first) + tail)

@KtorExperimentalAPI
@JvmName("loadApplicationConfigFromPaths")
fun ConfigLoader.loadApplicationEngineEnvironment(paths: List<Path>): ApplicationEngineEnvironment {
  val node = loadNodeOrThrow(paths)
  return hopliteApplicationEngineEnvironment(node)
}

fun hopliteApplicationEngineEnvironment(node: Value): ApplicationEngineEnvironment = applicationEngineEnvironment {

  val hostConfigPath = "ktor.deployment.host"
  val portConfigPath = "ktor.deployment.port"
  val applicationIdPath = "ktor.application.id"

  val applicationId = when (val n = node.atPath(applicationIdPath)) {
    is StringValue -> n.value
    is UndefinedValue -> "Application"
    else -> throw RuntimeException("Invalid value for $applicationIdPath")
  }

  log = LoggerFactory.getLogger(applicationId)
  config = HopliteApplicationConfig(node)

  connector {
    host = when (val n = node.atPath(hostConfigPath)) {
      is StringValue -> n.value
      is UndefinedValue -> "0.0.0.0"
      else -> throw RuntimeException("Invalid value for host: $n")
    }
    port = when (val n = node.atPath(portConfigPath)) {
      is LongValue -> n.value.toInt()
      is StringValue -> n.value.toInt()
      else -> throw RuntimeException("$portConfigPath is not defined or is not a number")
    }
  }
}

