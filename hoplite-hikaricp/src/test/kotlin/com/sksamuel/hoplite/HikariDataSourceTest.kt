package com.sksamuel.hoplite

import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class HikariDataSourceTest : StringSpec() {
  init {
    "hikari datasource decoder" {
      data class Config(val db: HikariDataSource)

      shouldThrowAny {
        ConfigLoader().loadConfigOrThrow<Config>("/hikari.yaml").db
      }.cause?.cause?.message shouldContain "serverhost"
    }
  }
}
