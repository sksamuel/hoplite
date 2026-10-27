package com.sksamuel.hoplite

import com.sksamuel.hoplite.yaml.YamlPropertySource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class HikariDataSourceTest : StringSpec() {
  init {
    "hikari datasource decoder" {
      data class Config(val db: HikariDataSource)

      shouldThrow<HikariPool.PoolInitializationException> {
        ConfigLoader().loadConfigOrThrow<Config>("/hikari.yaml").db
      }.cause?.cause?.message shouldContain "serverhost"
    }

    // gh-602: values passed through to Hikari are still checked for unresolved substitutions
    "hikari datasource decoder reports unresolved substitutions" {
      data class Config(val db: HikariDataSource)

      shouldThrow<ConfigException> {
        ConfigLoaderBuilder.defaultWithoutPropertySources()
          .addSource(
            YamlPropertySource(
              """
              db:
                dataSourceClassName: org.postgresql.ds.PGSimpleDataSource
                dataSource:
                  serverName: ${'$'}{NOPE_602_HOST}
              """.trimIndent()
            )
          )
          .build()
          .loadConfigOrThrow<Config>()
      }.message shouldContain "Unresolved substitution \${NOPE_602_HOST}"
    }
  }
}
