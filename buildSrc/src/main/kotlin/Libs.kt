object Libs {

  const val kotlinVersion = "1.5.0"
  const val org = "com.sksamuel.hoplite"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.2.0"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinVersion"
  }

  object Arrow {
    const val Core = "io.arrow-kt:arrow-core:0.13.2"
  }

  object Aws {
    private const val version = "1.12.36"
    const val core = "com.amazonaws:aws-java-sdk-core:$version"
    const val ssm = "com.amazonaws:aws-java-sdk-ssm:$version"
    const val secrets = "com.amazonaws:aws-java-sdk-secretsmanager:$version"
  }

  object CronUtils {
    const val utils = "com.cronutils:cron-utils:9.1.3"
  }

  object Hadoop {
    const val common = "org.apache.hadoop:hadoop-common:2.10.1"
  }

  object Hikari {
    const val core = "com.zaxxer:HikariCP:4.0.3"
  }

  object Typesafe {
    const val config = "com.typesafe:config:1.4.1"
  }

  object Jackson {
    const val core = "com.fasterxml.jackson.core:jackson-core:2.12.3"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.12.3"
  }

  object Kotest {
    private const val version = "5.1.0"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }

  object Orbitz {
    const val consul = "com.orbitz.consul:consul-client:1.5.3"
  }

  object Postgres {
    const val driver = "org.postgresql:postgresql:42.2.20"
  }

  object Snake {
    private const val version = "1.28"
    const val snakeyml = "org.yaml:snakeyaml:$version"
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
