package com.sksamuel.hoplite

import arrow.data.valid
import com.sksamuel.hoplite.arrow.ap
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.loadProps
import java.util.*

/**
 * A [PropertySource] provides [TreeNode]s.
 * A source may retrieve its values from a config file, or env variables, and so on.
 */
interface PropertySource {
  fun node(): ConfigResult<TreeNode>
}

fun defaultPropertySources(): List<PropertySource> =
  listOf(
    SystemPropertiesPropertySource
    //   JndiPropertySource,
//    EnvironmentVaraiblesPropertySource
    //  UserSettingsPropertySource
  )

object SystemPropertiesPropertySource : PropertySource {
  override fun node(): ConfigResult<TreeNode> = loadProps(System.getProperties(), "sysprops").valid()
}

object JndiPropertySource

object EnvironmentVariablesPropertySource : PropertySource {
  override fun node(): ConfigResult<TreeNode> {
    val props = Properties()
    System.getenv().forEach { props[it.key] = it.value }
    return loadProps(props, "envvars").valid()
  }
}

object UserSettingsPropertySource


/**
 * An implementation of [PropertySource] that loads values from a file located
 * via a [FileSource]. The file is parsed using an instance of [Parser] retrieved
 * from the [ParserRegistry] based on file extension.
 */
class ConfigFilePropertySource(private val file: FileSource,
                               private val parserRegistry: ParserRegistry) : PropertySource {
  override fun node(): ConfigResult<TreeNode> {
    val parser = parserRegistry.locate(file.ext())
    val input = file.open()
    return ap(parser, input) {
      it.a.load(it.b, file.describe()).valid()
    }
  }
}

