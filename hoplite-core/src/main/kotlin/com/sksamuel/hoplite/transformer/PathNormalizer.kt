package com.sksamuel.hoplite.transformer

import com.sksamuel.hoplite.*

/**
 * To support loading configuration from a tree based on multiple sources with different idiomatic conventions, such
 * as HOCON which prefers kebab case, and environment variables which are upper-case, the path normalizer normalizes
 * all paths so that the cascade happens correctly. For example, a `foo.conf` containing the HOCON standard naming
 * of `abc.foo-bar` and an env var `ABC_FOOBAR` would both get mapped to data class `Foo { val fooBar: String }`
 * assuming there is a Lowercase parameter mapper present.
 *
 * Note that with path normalization, parameters with the same name but different case will be considered the same,
 * and assigned the same value. This should generally be a situation one should avoid, but if it does happen, please
 * consider the use of the @[ConfigAlias] annotation to disambiguate the properties.
 *
 * Path normalization does the following for all node keys and each element of each node's path:
 * * Removes dashes
 * * Removes underscores
 * * Converts to lower-case
 *
 * It does NOT normalize the sealed type discriminator field for map nodes.
 */
object PathNormalizer : NodeTransformer {
  override fun transformPathElement(element: String): String = element
    .replace("-", "")
    .replace("_", "")
    .lowercase()

  override fun transform(node: Node, sealedTypeDiscriminatorField: String?): Node = node
    .transform {
      val normalizedPathNode = it.withPath(
        it.path.copy(keys = it.path.keys.map { key ->
          if (it is MapNode) normalizePathElementExceptDiscriminator(key, sealedTypeDiscriminatorField)
          else transformPathElement(key)
        })
      )
      when (normalizedPathNode){
        is MapNode -> normalizedPathNode.copy(map = normalizedPathNode.map.mapKeys { (key, _) ->
          val normalizedKey = normalizePathElementExceptDiscriminator(key, sealedTypeDiscriminatorField)
          // if normalization would cause overwriting an existing key, then don't normalize it
          // this can be relevant for writing config into Maps
          if (normalizedPathNode.map.containsKey(normalizedKey)) key else normalizedKey
        })
        else -> normalizedPathNode
      }
    }

  private fun normalizePathElementExceptDiscriminator(element: String, sealedTypeDiscriminatorField: String?): String {
    return if (sealedTypeDiscriminatorField != null && element == sealedTypeDiscriminatorField) element
    else element
      .replace("-", "")
      .replace("_", "")
      .lowercase()
  }
}
