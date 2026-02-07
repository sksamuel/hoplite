package com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading

import com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading.ConfigLoader.loadUsingCustomClassLoader
import com.sksamuel.hoplite.gh511.classpathLoadingAndFileLoading.ConfigLoader.loadUsingHopliteClassloader
import io.kotest.core.spec.style.AnnotationSpec

@Suppress("NonAsciiCharacters")
class ConfigLoaderTest : AnnotationSpec() {

  data class Database(val dbJdbcUrl: String, val username: String)
  data class Config(val gh511Database: Database, val gh511Env: String)

  @Test
  fun `customClassloader application-properties`() {
    val c = loadUsingCustomClassLoader<Config>(listOf("gh511-application.properties"))
  }

  @Test
  fun `customClassloader envfile`() {
    val c = loadUsingCustomClassLoader<Config>(listOf(".gh511-envfile"))
  }

  @Test
  fun `customClassloader nested-application-properties`() {
    val c = loadUsingCustomClassLoader<Config>(listOf("nested/gh511-nested-application.properties"))
  }

  // /**
  //  * the problem is, using hoplite's current implementation, for _some_ files i have to put `/` in front,
  //  * while for other files, i do not need to put `/` in front.
  //  *
  //  * Why this duality?
  //  * Can hoplite be fixed?
  //  *
  //  * In my opinion, both `.envfile` and `application.properties` should be found without the `/`, as they're top level resources.
  //  */
  @Test
  fun `hopliteClassloader application-properties ❌`() {
    val c = loadUsingHopliteClassloader<Config>(listOf("gh511-application.properties"))
  }

  @Test
  fun `hopliteClassloader application-properties ✅ `() {
    val c = loadUsingHopliteClassloader<Config>(listOf("/gh511-application.properties"))
  }

  /**
   * This is the only one which works without a / in front.
   *
   * This makes setup confusing!
   */
  @Test
  fun `hopliteClassloader envfile`() {
    val c = loadUsingHopliteClassloader<Config>(listOf(".gh511-envfile"))
  }

  @Test
  fun `hopliteClassloader nested-application-properties`() {
    val c = loadUsingHopliteClassloader<Config>(listOf("/nested/gh511-nested-application.properties"))
  }


  @Test
  fun `expected should support both slash and no slash in all cases`() {
    loadUsingHopliteClassloader<Config>(listOf("gh511-application.properties"))
    loadUsingHopliteClassloader<Config>(listOf("/gh511-application.properties"))
    loadUsingHopliteClassloader<Config>(listOf(".gh511-envfile"))
    loadUsingHopliteClassloader<Config>(listOf("/.gh511-envfile"))
    loadUsingHopliteClassloader<Config>(listOf("/nested/gh511-nested-application.properties"))
    loadUsingHopliteClassloader<Config>(listOf("nested/gh511-nested-application.properties"))
  }
}
