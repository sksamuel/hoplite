package com.sksamuel.hoplite

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.arrow.ap
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.loadProps

/**
 * Represents a path to a config value.
 */
data class Key(val components: List<String>) {
  companion object {
    val root = Key(emptyList())
  }
}

/**
 * A [PropertySource] provides [Value]s.
 * A source may retrieve its values from a config file, or env variables, and so on.
 */
interface PropertySource {

  /**
   * Looks up a value in this source at the given key.
   */
  fun lookup(key: Key): ConfigResult<Value> = ConfigFailure.NoSuchParser("todo").invalid()

  fun node(): ConfigResult<Value>
}

fun defaultPropertySources(): List<PropertySource> =
  listOf(
    SystemPropertiesPropertySource
    //   JndiPropertySource,
//    EnvironmentVaraiblesPropertySource
    //  UserSettingsPropertySource
  )

object SystemPropertiesPropertySource : PropertySource {
  override fun lookup(key: Key): ConfigResult<Value> {
    val k = key.join(".")
    return when (val value = System.getProperties().getProperty(k)) {
      null -> UndefinedValue(Pos.NoPos, k).valid()
      else -> StringValue(value, Pos.NoPos, k).valid()
    }
  }
  override fun node(): ConfigResult<Value> = loadProps(System.getProperties(), "sysprops").valid()
}

fun Key.join(sep: String): String = components.joinToString(sep)

object JndiPropertySource : PropertySource {
  override fun node(): ConfigResult<Value> {
    TODO()
  }
}

object EnvironmentVariablesPropertySource : PropertySource {
  override fun lookup(key: Key): ConfigResult<Value> {
    val k = key.join(".")
    return when (val value = System.getenv()[k]) {
      null -> UndefinedValue(Pos.NoPos, k).valid()
      else -> StringValue(value, Pos.NoPos, k).valid()
    }
  }
  override fun node(): ConfigResult<Value> = loadProps(System.getProperties(), "sysprops").valid()
}

object UserSettingsPropertySource : PropertySource {
  override fun node(): ConfigResult<Value> {
    TODO()
  }
}

/**
 * An implementation of [PropertySource] that loads values from a file located
 * via a [FileSource]. The file is parsed using an instance of [Parser] retrieved
 * from the [ParserRegistry] based on file extension.
 */
class ConfigFilePropertySource(private val file: FileSource,
                               private val parserRegistry: ParserRegistry) : PropertySource {

  override fun node(): ConfigResult<Value> = ap(parserRegistry.locate(file.ext()), file.open()) {
    it.a.load(it.b, file.describe())
  }
}
