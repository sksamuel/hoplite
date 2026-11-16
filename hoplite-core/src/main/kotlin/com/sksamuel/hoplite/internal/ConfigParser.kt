package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ClasspathResourceLoader
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.DecoderConfig
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LookupPropertySource
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.transform
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.ParameterMapper
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.transformer.NodeTransformer
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.resolver.Resolving
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.transformer.PathNormalizer
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

class ConfigParser(
  classpathResourceLoader: ClasspathResourceLoader,
  private val parserRegistry: ParserRegistry,
  private val allowEmptyTree: Boolean,
  allowNullOverride: Boolean,
  cascadeMode: CascadeMode,
  preprocessors: List<Preprocessor>,
  preprocessingIterations: Int,
  private val nodeTransformers: List<NodeTransformer>,
  private val resolvers: List<Resolver>,
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val flattenArraysToString: Boolean,
  private val resolveTypesCaseInsensitive: Boolean,
  private val allowUnresolvedSubstitutions: Boolean,
  private val secretsPolicy: SecretsPolicy?,
  private val decodeMode: DecodeMode,
  strictPrefixedSources: Boolean,
  private val useReport: Boolean,
  private val obfuscator: Obfuscator,
  private val reportPrintFn: Print,
  private val environment: Environment?,
  private val sealedTypeDiscriminatorField: String?,
  private val contextResolverMode: ContextResolverMode,
  private val resourceOrFiles: List<String>,
  private val propertySources: List<PropertySource>,
  private val configSources: List<ConfigSource>,
) {

  private val loader = PropertySourceLoader(nodeTransformers, sealedTypeDiscriminatorField, classpathResourceLoader, parserRegistry, allowEmptyTree)
  private val decoding = Decoding(decoderRegistry, secretsPolicy, strictPrefixedSources)

  // Nothing is read until the first decode or load, which reads every source; for a ConfigBinder, the first bind.

  // lookup sources look up values per bind (gh-606), each from the snapshot it takes on first use
  private val sources by lazy { propertySources.map { if (it is LookupPropertySource) it.snapshot() else it } }
  private val hasLookups = propertySources.any { it is LookupPropertySource }

  // with lookup sources the tree may be empty, as they may provide every value
  private val cascader = Cascader(cascadeMode, allowEmptyTree || hasLookups, allowNullOverride, sealedTypeDiscriminatorField)

  // With lookup sources, each bind builds and preprocesses its own config tree, file values included. Preprocessors
  // that handle one value at a time cache their result per value, so a secret isn't fetched again and a random value
  // doesn't change. Those that read the whole tree, such as LookupPreprocessor, can't be cached per value, as their
  // result depends on the rest of the tree, so they run on every bind.
  private val preprocessing = Preprocessing(preprocessors, preprocessingIterations, cacheValueResults = hasLookups)

  /**
   * One source's part of the config. The layers are in precedence order, highest first, and each bind cascades
   * the nodes they give for it.
   */
  private sealed interface Layer {
    /** A tree-based source, whose node is loaded and transformed once, on first use. */
    class Tree(val node: Node) : Layer

    /** A lookup source, whose node is built for each bind from the values the bound class needs. */
    class Lookup(val source: LookupPropertySource) : Layer
  }

  private val layers: ConfigResult<List<Layer>> by lazy { loadLayers() }

  // shared by all binds, as are the usages and report it records; each config tree gets its own resolvers
  private val baseContext = context(Undefined)

  /**
   * The config tree from the tree-based sources alone, and the context to decode it with. Without lookup sources it
   * is the whole config, built on first use. With them, it is the config of every bind for which no lookup source has
   * any value.
   */
  private val treeRoot: ConfigResult<Pair<Node, DecoderContext>> by lazy {
    layers.flatMap { layers -> root(layers.filterIsInstance<Layer.Tree>().map { it.node }) }
  }

  private fun context(root: Node): DecoderContext {
    return DecoderContext(
      decoders = decoderRegistry,
      paramMappers = paramMappers,
      nodeTransformers = nodeTransformers,
      config = DecoderConfig(flattenArraysToString, resolveTypesCaseInsensitive),
      environment = environment,
      resolvers = Resolving(resolvers, root),
      sealedTypeDiscriminatorField = sealedTypeDiscriminatorField,
      contextResolverMode = contextResolverMode,
      // Checked as values are decoded rather than over the whole tree at load, so values the bound class never
      // refers to (such as shell variables from the environment source) can't fail the load (gh-602).
      // If resolvers is not empty, we don't use allowUnresolvedSubstitutions, but instead the resolver mode setting.
      checkUnresolvedSubstitutions = resolvers.isEmpty() && !allowUnresolvedSubstitutions &&
        contextResolverMode != ContextResolverMode.SkipUnresolved,
    )
  }

  fun <A : Any> decode(
    kclass: KClass<A>,
    environment: Environment?,
    prefix: String? = null,
  ): ConfigResult<A> {

    return bindRoot(kclass, prefix)
      .flatMap { (root, context) ->
        val it = root.prefixedNode(prefix)
        // Mark the prefixed subtree root as used so strict mode does not report it as unused.
        // The path is read from the node itself (not the raw prefix string), which ensures
        // compatibility with PathNormalizer.
        if (prefix != null && it !is Undefined) {
          context.usedPaths.add(it.path)
        }
        val decoded = decoding.decode(kclass, it, decodeMode, context)
        val state = createDecodingState(it, context, secretsPolicy)

        // always do report regardless of decoder result
        if (useReport) {
          Reporter(reportPrintFn, obfuscator, environment, prefix)
            .printReport(propertySources, state, context.reporter.getReport())
        }

        decoded
      }
  }

  /**
   * Returns the config tree, with the values of the lookup sources imported in full, as [LookupPropertySource.node]
   * provides them, since there is no target type to look up keys for.
   */
  fun load(): ConfigResult<Node> = fullRoot

  private val fullRoot: ConfigResult<Node> by lazy {
    layers.flatMap { layers ->
      layers.map { layer ->
        when (layer) {
          is Layer.Tree -> layer.node.valid()
          is Layer.Lookup -> layer.source.node(PropertySourceContext(parserRegistry, allowEmptyTree)).map { transform(it) }
        }
      }.sequence().mapInvalid { ConfigFailure.MultipleFailures(it) }
    }.flatMap { root(it) }.map { it.first }
  }

  /**
   * Returns the config tree for one bind of [kclass] at [prefix]: each lookup source's values for the class, in
   * its place among the tree-based sources.
   */
  private fun bindRoot(kclass: KClass<*>, prefix: String?): ConfigResult<Pair<Node, DecoderContext>> =
    layers.flatMap { layers ->
      val type = kclass.starProjectedType
      val nodes = layers.map { layer ->
        when (layer) {
          is Layer.Tree -> layer.node
          is Layer.Lookup -> transform(LookupTreeBuilder(layer.source, baseContext).build(type, prefix))
        }
      }
      val lookedUp = layers.zip(nodes).any { (layer, node) -> layer is Layer.Lookup && node !is Undefined }
      if (lookedUp) root(nodes) else treeRoot
    }

  /**
   * Cascades [nodes], in precedence order, then preprocesses the result: the config tree, and the context to
   * decode it with.
   */
  private fun root(nodes: List<Node>): ConfigResult<Pair<Node, DecoderContext>> =
    cascader.cascade(NonEmptyList.unsafe(nodes.ifEmpty { listOf(Undefined) })).flatMap { node ->
      // lookup trees keep lists as maps keyed by index through the cascade
      val root = LookupTreeBuilder.compactLists(node)
      val context = baseContext.copy(resolvers = Resolving(resolvers, root))
      preprocessing.preprocess(root, context).map { it to context }
    }

  /**
   * Returns a layer for every source, in precedence order: the property sources in order, each lookup source as a
   * [Layer.Lookup] and each tree-based one as a [Layer.Tree] of its loaded node, then a [Layer.Tree] for each config
   * source, file and resource.
   */
  private fun loadLayers(): ConfigResult<List<Layer>> {
    if (decoderRegistry.size == 0) return ConfigFailure.EmptyDecoderRegistry.invalid()
    return loadTreeNodes().map { nodes ->
      val treeNodes = nodes.iterator()
      sources.map { if (it is LookupPropertySource) Layer.Lookup(it) else Layer.Tree(treeNodes.next()) } +
        treeNodes.asSequence().map { Layer.Tree(it) }
    }
  }

  /**
   * Loads the nodes of the tree-based property sources, in order, then of the config sources, files and resources.
   */
  private fun loadTreeNodes(): ConfigResult<List<Node>> {
    val treeSources = sources.filterNot { it is LookupPropertySource }
    // with only lookup sources there is nothing to load, and loadNodes requires at least one source
    if (sources.isNotEmpty() && treeSources.isEmpty() && configSources.isEmpty() && resourceOrFiles.isEmpty())
      return emptyList<Node>().valid()
    return loader.loadNodes(treeSources, configSources, resourceOrFiles).map { it.list }
  }

  private fun transform(node: Node): Node = nodeTransformers.fold(node) { acc, transformer ->
    acc.transform { transformer.transform(it, sealedTypeDiscriminatorField) }
  }

  private fun Node.prefixedNode(prefix: String?) = when {
    prefix == null -> this
    nodeTransformers.contains(PathNormalizer) -> atPath(PathNormalizer.transformPathElement(prefix))
    else -> atPath(prefix)
  }
}
