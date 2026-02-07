package com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceOrFileSource
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.sources.ConfigFilePropertySource
import java.nio.file.Paths
import kotlin.io.path.exists

object ConfigLoader {
    @OptIn(ExperimentalHoplite::class)
    inline fun <reified T : Any> loadUsingCustomClassLoader(propertiesFiles: List<String>): T {
        val builder = ConfigLoaderBuilder.default()
        val classpathLoader = Thread.currentThread().contextClassLoader.toClasspathResourceLoader()
        builder
            .withExplicitSealedTypes()
            .withReport()
        propertiesFiles.forEach { f ->
            val source = Paths.get(f).takeIf { it.exists() }
                ?.let { ConfigSource.PathSource(it) }
                ?: ConfigSource.ClasspathSource(f, classpathLoader)
            builder.addPropertySource(ConfigFilePropertySource(source, allowEmpty = false))
            builder.addFileExtensionMapping(f.substringAfterLast("."), PropsParser())
        }
        return builder.build().loadConfigOrThrow<T>()
    }

    @OptIn(ExperimentalHoplite::class)
    inline fun <reified T : Any> loadUsingHopliteClassloader(propertiesFiles: List<String>): T {
        val builder = ConfigLoaderBuilder.default()
        builder
            .withExplicitSealedTypes()
            .withReport()

        propertiesFiles.forEach { f ->
            builder.addResourceOrFileSource(f)
            builder.addFileExtensionMapping(f.substringAfterLast("."), PropsParser())
        }

        return builder.build().loadConfigOrThrow<T>()
    }
}
