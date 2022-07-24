object Libs {

  const val kotlinVersion = "_"
  const val org = "com.sksamuel.hoplite"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:_"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:_"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:_"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:_"
  }

  object Arrow {
    const val Core = "io.arrow-kt:arrow-core:_"
  }

  object Aws {
    private const val version = "_"
    const val core = "com.amazonaws:aws-java-sdk-core:_"
    const val ssm = "com.amazonaws:aws-java-sdk-ssm:_"
    const val secrets = "com.amazonaws:aws-java-sdk-secretsmanager:_"
  }

  object Aws2 {
    private const val version = "_"
    const val regions = "software.amazon.awssdk:regions:_"
  }

  object CronUtils {
    const val utils = "com.cronutils:cron-utils:_"
  }

  object Hadoop {
    const val common = "org.apache.hadoop:hadoop-common:_"
  }

  object Hikari {
    const val core = "com.zaxxer:HikariCP:_"
  }

  object Micrometer {
    const val datadog = "io.micrometer:micrometer-registry-datadog:_"
    const val prometheus = "io.micrometer:micrometer-registry-prometheus:_"
    const val statsd = "io.micrometer:micrometer-registry-statsd:_"
  }

  object Typesafe {
    const val config = "com.typesafe:config:_"
  }

  object Jackson {
    private const val version = "_"
    const val core = "com.fasterxml.jackson.core:jackson-core:_"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:_"
  }

  object Kotest {
    private const val version = "_"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:_"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:_"
    const val testContainers = "io.kotest.extensions:kotest-extensions-testcontainers:_"
  }

  object Orbitz {
    const val consul = "com.orbitz.consul:consul-client:_"
  }

  object Postgres {
    const val driver = "org.postgresql:postgresql:_"
  }

  object Snake {
    const val snakeyaml = "org.yaml:snakeyaml:_"
  }

  object Toml {
    const val toml = "org.tomlj:tomlj:_"
  }

  object Vavr {
    const val kotlin = "io.vavr:vavr-kotlin:_"
  }

  object Vault {
    const val core = "org.springframework.vault:spring-vault-core:2.3.2"
  }

  object EmbeddedConsul {
    const val consul = "com.pszymczyk.consul:embedded-consul:_"
  }
}
