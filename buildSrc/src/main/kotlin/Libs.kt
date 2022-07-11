object Libs {

  const val kotlinVersion = "1.6.21"
  const val org = "com.sksamuel.hoplite"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3"
  }

  object Arrow {
    const val Core = "io.arrow-kt:arrow-core:1.1.2"
  }

  object Aws {
    private const val version = "1.12.253"
    const val core = "com.amazonaws:aws-java-sdk-core:$version"
    const val ssm = "com.amazonaws:aws-java-sdk-ssm:$version"
    const val secrets = "com.amazonaws:aws-java-sdk-secretsmanager:$version"
  }

  object Aws2 {
    private const val version = "2.17.168"
    const val regions = "software.amazon.awssdk:regions:$version"
  }

  object CronUtils {
    const val utils = "com.cronutils:cron-utils:9.1.6"
  }

  object Hadoop {
    const val common = "org.apache.hadoop:hadoop-common:2.10.1"
  }

  object Hikari {
    const val core = "com.zaxxer:HikariCP:5.0.1"
  }

  object Micrometer {
    const val datadog = "io.micrometer:micrometer-registry-datadog:1.9.1"
    const val prometheus = "io.micrometer:micrometer-registry-prometheus:1.9.1"
    const val statsd = "io.micrometer:micrometer-registry-statsd:1.9.1"
  }

  object Typesafe {
    const val config = "com.typesafe:config:1.4.2"
  }

  object Jackson {
    private const val version = "2.13.3"
    const val core = "com.fasterxml.jackson.core:jackson-core:$version"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.13.2.2"
  }

  object Kotest {
    private const val version = "5.3.2"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
    const val testContainers = "io.kotest.extensions:kotest-extensions-testcontainers:1.2.1"
  }

  object Orbitz {
    const val consul = "com.orbitz.consul:consul-client:1.5.3"
  }

  object Postgres {
    const val driver = "org.postgresql:postgresql:42.4.0"
  }

  object Snake {
    const val snakeyaml = "org.yaml:snakeyaml:1.30"
  }

  object Toml {
    const val toml = "org.tomlj:tomlj:1.0.0"
  }

  object Vavr {
    const val kotlin = "io.vavr:vavr-kotlin:0.10.2"
  }

  object EmbeddedConsul {
    const val consul = "com.pszymczyk.consul:embedded-consul:2.2.1"
  }
}
