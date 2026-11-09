package com.sksamuel.hoplite

import com.sksamuel.hoplite.yaml.YamlPropertySource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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

    "strict mode accepts consumed Hikari properties" {
      val report = StringBuilder()
      try {
        val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
          .addSource(
            YamlPropertySource(
              """
              db:
                dataSourceClassName: org.postgresql.ds.PGSimpleDataSource
                initializationFailTimeout: -1
                minimumIdle: 0
                maximumPoolSize: 2
                dataSource:
                  serverName: localhost
                  databaseName: example
              """.trimIndent()
            )
          )
          .strict()
          .withReport()
          .withReportPrintFn { report.append(it) }
          .build()
          .loadConfigOrThrow<StrictConfig>()

        config.db.maximumPoolSize shouldBe 2
        config.db.dataSourceProperties["databaseName"] shouldBe "example"
        report.toString() shouldContain "Unused keys: none"
      } finally {
        StrictConfig.datasource?.close()
        StrictConfig.datasource = null
      }
    }

    "strict mode still rejects Hikari nodes that are not consumed" {
      try {
        shouldThrow<ConfigException> {
          ConfigLoaderBuilder.defaultWithoutPropertySources()
            .addSource(
              YamlPropertySource(
                """
                db:
                  dataSourceClassName: org.postgresql.ds.PGSimpleDataSource
                  initializationFailTimeout: -1
                  minimumIdle: 0
                  connectionInitSql: [unused]
                """.trimIndent()
              )
            )
            .strict()
            .build()
            .loadConfigOrThrow<StrictConfig>()
        }.message shouldContain "db.connectionInitSql"
      } finally {
        StrictConfig.datasource?.close()
        StrictConfig.datasource = null
      }
    }
  }

  data class StrictConfig(val db: HikariDataSource) {
    init {
      datasource = db
    }

    companion object {
      var datasource: HikariDataSource? = null
    }
  }
}
