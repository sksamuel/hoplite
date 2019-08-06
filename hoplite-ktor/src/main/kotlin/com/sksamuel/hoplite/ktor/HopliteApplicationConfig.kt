package com.sksamuel.hoplite.ktor

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
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
    else -> throw IllegalArgumentException("$node cannot be converted to string")
  }

  override fun getList(): List<String> = when (node) {
    is ListNode -> node.elements.map {
      when (node) {
        is PrimitiveNode -> node.value.toString()
        else -> throw IllegalArgumentException("$node cannot be converted to string")
      }
    }
    else -> throw IllegalArgumentException("$node cannot be converted to list")
  }
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationConfig(vararg resources: String): ApplicationConfig {
  val node = loadNodeOrThrow(* resources)
  return HopliteApplicationConfig(node)
}

@KtorExperimentalAPI
fun ConfigLoader.loadApplicationConfig(vararg paths: Path): ApplicationConfig {
  val node = loadNodeOrThrow(* paths)
  return HopliteApplicationConfig(node)
}
