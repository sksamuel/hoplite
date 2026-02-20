package com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceOrFileSource
import com.sksamuel.hoplite.parsers.PropsParser
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec

class SmarterPathAndClasspathLoadingTest : AnnotationSpec() {

  data class Database(val dbJdbcUrl: String, val username: String)
  data class Config(val gh511Database: Database, val gh511Env: String)

  @Test
  fun `classpath no slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf("gh511-application.properties")) }
  }

  @Test
  fun `classpath with slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf("/gh511-application.properties")) }
  }

  @Test
  fun `nested classpath no slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf("nested/gh511-nested-application.properties")) }
  }

  @Test
  fun `nested classpath with slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf("/nested/gh511-nested-application.properties")) }
  }

  @Test
  fun `simple path no slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf(".gh511-envfile")) }
  }

  @Test
  fun `simple path with slash`() {
    shouldNotThrowAny { loadConfig<Config>(listOf("/.gh511-envfile")) }
  }

  @OptIn(ExperimentalHoplite::class)
  private inline fun <reified T : Any> loadConfig(propertiesFiles: List<String>): T {
    val builder = ConfigLoaderBuilder.default().withExplicitSealedTypes()

    propertiesFiles.forEach { f ->
      builder.addResourceOrFileSource(f)
      builder.addFileExtensionMapping(f.substringAfterLast("."), PropsParser())
    }

    return builder.build().loadConfigOrThrow<T>()
  }

}
