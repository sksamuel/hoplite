package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.ParserRegistry

/**
 * A [PropertySource] provides [Node]s.
 *
 * A property source may retrieve its values from a config file, or env variables, system properties, and so on,
 * depending on the implementation.
 */
interface PropertySource {

  /**
   * Returns the node associated with this property source
   */
  fun node(parsers: ParserRegistry): ConfigResult<Node>

  companion object {

    /**
     * Returns a [PropertySource] that will read from the specified string.
     *
     * @param str the string to read from
     * @param ext the file extension of the input format
     */
    fun string(str: String, ext: String) = ByteArrayPropertySource(str.encodeToByteArray(), ext)
  }
}

/**
 * An implementation of [PropertySource] that provides config via a byte array.
 * You must specify the config type via a registered extension.
 *
 * @param bytes the bytes that contains the config.
 *
 * @param ext the file extension that will be used in the parser registry to locate the
 * correct parser to use. For example, pass in "yml" if the input stream represents a yml file.
 * It is important the right extension type is passed in, because the input stream doesn't itself
 * offer any indication what type of file it contains.
 */
class ByteArrayPropertySource(
  private val bytes: ByteArray,
  private val ext: String
) : PropertySource {

  override fun node(parsers: ParserRegistry): ConfigResult<Node> {
    return parsers.locate(ext).map {
      it.load(bytes, "byte-array-source")
    }
  }
}
