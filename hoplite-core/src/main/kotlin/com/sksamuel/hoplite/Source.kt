package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatRecover
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

/**
 * A [PropsSource] provides a tree of config values in the form of a [Props] instance.
 */
interface PropsSource {
  fun props(context: PropertySourceContext): ConfigResult<Props>
}

/**
 * A tree of config values.
 */
interface Props {

  /**
   * Returns the [Value] at the given [Path].
   *
   * The value returned will be [Undefined] if the path is not present
   * in this source.
   */
  fun at(path: Path): Value
}

data class Path(val components: List<String>) {
  fun append(name: String): Path = Path(components + name)
  fun parent(): Path = Path(components.dropLast(1))
  fun asString(separator: String): String = components.joinToString(separator)
}

object EmptyProps : Props {
  override fun at(path: Path): Value = Value.UndefinedValue
}

sealed interface Value {

  // complex

  data class MapNode(
    val map: Map<String, Node>,
    override val pos: Pos,
    val value: Node = Undefined
  ) : ContainerNode() {
    override val simpleName: String = "Map"
    override fun atKey(key: String): Node = map[key] ?: Undefined
    override fun atIndex(index: Int): Node = Undefined
    override val size: Int = map.size
  }

  data class ArrayNode(
    val elements: List<Node>,
    override val pos: Pos
  ) : ContainerNode() {
    override val simpleName: String = "List"
    override fun atKey(key: String): Node = Undefined
    override fun atIndex(index: Int): Node = elements.getOrElse(index) { Undefined }
    override val size: Int = elements.size
  }

  // primitives
  data class StringValue(val value: String) : Value
  data class DoubleValue(val value: Double) : Value
  data class LongValue(val value: Long) : Value
  data class IntValue(val value: Int) : Value
  data class BooleanValue(val value: Boolean) : Value
  object NullValue : Value
  object UndefinedValue : Value
}

/**
 * A [PropsSource] that loads values from a config file, provided by a [ConfigSource].
 *
 * The file is parsed using a [Parser] that is retrieved from the [ParserRegistry]
 * based on file extension.
 *
 * @param optional if true then a missing file will be skipped.
 *                 if false, then a missing file will return an error.
 *                 Defaults to false.
 */
class ConfigFilePropsSource(
  private val config: ConfigSource,
  private val optional: Boolean = false,
) : PropsSource {
  override fun props(context: PropertySourceContext): ConfigResult<Props> {
    val parser = context.parsers.locate(config.ext())
    val input = config.open()
    return Validated.mapN(parser, input) { a, b -> ConfigFileProps(a.load(b, config.describe())) }
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatRecover { if (optional) EmptyProps.valid() else it.invalid() }
  }
}

class ConfigFileProps(load: Node) : Props {
  override fun at(path: Path): Value {
    return Value.UndefinedValue
  }
}

class EnvVarsPropsSource(
  private val useUnderscoresAsSeparator: Boolean,
  private val caseInsensitive: Boolean,
) : PropsSource {
  override fun props(context: PropertySourceContext): ConfigResult<Props> =
    EnvVarsProps(useUnderscoresAsSeparator, caseInsensitive).valid()
}

class EnvVarsProps(
  private val useUnderscoresAsSeparator: Boolean,
  private val caseInsensitive: Boolean,
) : Props {
  override fun at(path: Path): Value {
    val key = path.asString(if (useUnderscoresAsSeparator) "__" else ".")
    val value = System.getenv().toList().firstOrNull { it.first.equals(key, caseInsensitive) }?.second
    return if (value == null) Value.UndefinedValue else Value.StringValue(value)
  }
}

/**
 * A [PropsSource] that provides config through system properties
 * that are prefixed with 'config.override.'
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 */
object SystemPropertiesPropsSource : PropsSource {
  override fun props(context: PropertySourceContext): ConfigResult<Props> = SystemPropertiesProps.valid()
}

object SystemPropertiesProps : Props {
  private const val prefix = "config.override."
  override fun at(path: Path): Value {
    val key = prefix + path.asString(".")
    val value = System.getProperties()
      .stringPropertyNames()
      .find { it == key }
    return if (value == null) Value.UndefinedValue else Value.StringValue(value)
  }
}
