package com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceOrFileSource
import com.sksamuel.hoplite.parsers.PropsParser
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.engine.spec.tempdir

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

  @Test
  fun `absolute filesystem path is loaded as a file`() {
    // Write the same .properties content to an absolute path under tempdir, then load it
    // by its absolute path. Previously this would silently strip the leading slash and try
    // to find `tmp/.../foo.properties` relative to CWD, fail, and fall through to a
    // classpath lookup that didn't exist either.
    val tempDir = tempdir()
    val file = tempDir.resolve("absolute.properties")
    file.writeText(
      """
      gh511Database.dbJdbcUrl=jdbc:postgresql://localhost:5432/db
      gh511Database.username=admin
      gh511Env=test
      """.trimIndent()
    )
    shouldNotThrowAny { loadConfig<Config>(listOf(file.absolutePath)) }
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
