package com.sksamuel.hoplite

import java.io.InputStream

/**
 * An implementation of [PropertySource] that provides config via an [InputStream].
 * You must specify the config type in addition to the stream source.
 *
 * @param input the input stream that contains the config.
 *
 * @param ext the file extension that will be used in the parser registry to locate the
 * correct parser to use. For example, pass in "yml" if the input stream represents a yml file.
 * It is important the right extension type is passed in, because the input stream doesn't itself
 * offer any indication what type of file it contains.
 */
class InputStreamPropertySource(
   private val input: InputStream,
   private val ext: String
) : PropertySource {

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return context.parsers.locate(ext).map {
      it.load(input, "input-stream")
    }
  }
}
