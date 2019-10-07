package com.sksamuel.hoplite

import arrow.data.valid
import com.sksamuel.hoplite.arrow.ap
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.loadProps

interface PropertySource {
  fun node(): ConfigResult<Node>
}

fun defaultPropertySources(): List<PropertySource> =
  listOf(
    SystemPropertiesPropertySource
    //   JndiPropertySource,
//    EnvironmentVaraiblesPropertySource
    //  UserSettingsPropertySource
  )

object SystemPropertiesPropertySource : PropertySource {
  override fun node(): ConfigResult<Node> = loadProps(System.getProperties(), "sysprops").valid()
}

object JndiPropertySource : PropertySource {
  override fun node(): ConfigResult<Node> {
    TODO()
  }
}

object EnvironmentVaraiblesPropertySource : PropertySource {
  override fun node(): ConfigResult<Node> = TODO()
}

object UserSettingsPropertySource : PropertySource {
  override fun node(): ConfigResult<Node> {
    TODO()
  }
}

class ConfigFilePropertySource(private val file: FileSource,
                               private val parserRegistry: ParserRegistry) : PropertySource {

  override fun node(): ConfigResult<Node> = ap(parserRegistry.locate(file.ext()), file.open()) {
    it.a.load(it.b, file.describe())
  }
}
