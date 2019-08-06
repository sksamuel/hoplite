package com.sksamuel.hoplite.ktor

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue
import io.ktor.util.KtorExperimentalAPI
import java.lang.IllegalArgumentException
import java.nio.file.Path

@KtorExperimentalAPI
class HopliteApplicationConfig(private val node: Node) : ApplicationConfig {

  override fun config(path: String): ApplicationConfig = HopliteApplicationConfig(node.atKey(path))

  override fun configList(path: String): List<ApplicationConfig> = emptyList()

  override fun property(path: String): ApplicationConfigValue = HopliteApplicationConfigValue(node.atKey(path))

  override fun propertyOrNull(path: String): ApplicationConfigValue? =
    if (node.hasKeyAt(path)) property(path) else null

}

@KtorExperimentalAPI
class HopliteApplicationConfigValue(private val node: Node) : ApplicationConfigValue {

  override fun getString(): String = when (node) {
    is PrimitiveNode -> node.value.toString()
    else -> throw IllegalArgumentException("${node.simpleName} cannot be converted to string")
  }

  override fun getList(): List<String> = when (node) {
    is ListNode -> node.elements.map { element ->
      when (element) {
        is PrimitiveNode -> element.value.toString()
        else -> throw IllegalArgumentException("${element.simpleName} cannot be converted to string")
      }
    }
    is StringNode -> node.value.split(',').toList()
    else -> throw IllegalArgumentException("${node.simpleName} cannot be converted to list")
  }
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationConfig(first: String, vararg tail: String): ApplicationConfig =
  loadApplicationConfig(first + tail)

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationConfig(resources: List<String>): ApplicationConfig {
  val node = loadNodeOrThrow(resources)
  return HopliteApplicationConfig(node)
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationConfig(first: Path, vararg tail: Path): ApplicationConfig =
  loadApplicationConfig(first + tail)

@KtorExperimentalAPI
@JvmName("loadApplicationConfigFromPaths")
fun ConfigLoader.loadApplicationConfig(paths: List<Path>): ApplicationConfig {
  val node = loadNodeOrThrow(paths)
  return HopliteApplicationConfig(node)
}
