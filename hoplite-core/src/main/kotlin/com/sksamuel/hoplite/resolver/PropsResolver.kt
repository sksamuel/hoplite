package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import java.io.InputStream
import java.nio.file.Path
import java.util.Properties

/**
 * Resolves [Properties] from a file, input stream, or from a provided properties object,
 * and replaces strings of the form ${{ props.key }} with the value of that key in the properties object.
 *
 * When creating this class, you can specify the location of the properties file
 * either as a [Path] or as a resource on the classpath.
 */
class PropsResolver(private val props: Properties) : RegexResolverWithDefault() {

  // Redundant escaping required for Android support.
  override val regex = "\\$\\{(.*?)\\}".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode> {
    return props.getProperty(path) to node
  }

  companion object {

    operator fun invoke(resource: String): PropsResolver {
      val input = this::class.java.getResourceAsStream(resource)
        ?: throw ConfigException("Could not find resource $resource")
      return invoke(input)
    }

    operator fun invoke(input: InputStream): PropsResolver {
      val props = Properties().apply {
        input.use { this.load(it) }
      }
      return PropsResolver(props)
    }

    operator fun invoke(path: Path): PropsResolver =
      PropsResolver(path.toFile().inputStream())
  }
}
