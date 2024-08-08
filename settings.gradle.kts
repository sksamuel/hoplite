rootProject.name = "hoplite"

pluginManagement {
   repositories {
      mavenLocal()
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      }
      maven {
         url = uri("https://plugins.gradle.org/m2/")
      }
   }
}

include(
   ":hoplite-core",
   ":hoplite-azure",
   ":hoplite-aws",
   ":hoplite-aws2",
   ":hoplite-arrow",
   ":hoplite-consul",
   ":hoplite-cronutils",
   ":hoplite-datetime",
   ":hoplite-gcp",
   ":hoplite-hdfs",
   ":hoplite-hikaricp",
   ":hoplite-hocon",
   ":hoplite-javax",
   ":hoplite-json",
   ":hoplite-micrometer-datadog",
   ":hoplite-micrometer-prometheus",
   ":hoplite-micrometer-statsd",
   ":hoplite-toml",
   ":hoplite-vault",
   ":hoplite-vavr",
   ":hoplite-watch",
   ":hoplite-watch-consul",
   ":hoplite-yaml"
)

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
   versionCatalogs {
      create("libs") {

         library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:1.6.21")

         val micrometer = "1.12.8"
         library("micrometer-core", "io.micrometer:micrometer-core:$micrometer")
         library("micrometer-registry-datadog", "io.micrometer:micrometer-registry-datadog:$micrometer")
         library("micrometer-registry-prometheus", "io.micrometer:micrometer-registry-prometheus:$micrometer")
         library("micrometer-registry-statsd", "io.micrometer:micrometer-registry-statsd:$micrometer")

         val coroutines = "1.6.4"
         library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
         library("coroutines-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines")

         library("typesafe-config", "com.typesafe:config:1.4.2")

         library("arrow-core", "io.arrow-kt:arrow-core:1.1.3")

         library("spring-vault-core", "org.springframework.vault:spring-vault-core:2.3.4")

         library("vavr-kotlin", "io.vavr:vavr-kotlin:0.10.2")

         val aws1 = "1.12.566"
         library("aws-java-sdk-secretsmanager", "com.amazonaws:aws-java-sdk-secretsmanager:$aws1")
         library("aws-java-sdk-ssm", "com.amazonaws:aws-java-sdk-ssm:$aws1")

         library("cron-utils", "com.cronutils:cron-utils:9.2.1")

         library("hikaricp", "com.zaxxer:HikariCP:5.1.0")

         library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
         library("kotlinx-datetime", "org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

         library("google-cloud-secretmanager", "com.google.cloud:google-cloud-secretmanager:2.28.0")
         library("hadoop-common", "org.apache.hadoop:hadoop-common:2.10.2")
         library("tomlj", "org.tomlj:tomlj:1.1.1")
         library("embedded-consul", "com.pszymczyk.consul:embedded-consul:2.2.1")
         library("snakeyaml", "org.yaml:snakeyaml:2.2")

         library("slf4j-api", "org.slf4j:slf4j-api:2.0.9")

         val aws2 = "2.21.0"
         library("regions", "software.amazon.awssdk:regions:$aws2")
         library("secretsmanager", "software.amazon.awssdk:secretsmanager:$aws2")

         library("postgresql", "org.postgresql:postgresql:42.6.0")

         val jackson = "2.15.3"
         library("jackson-core", "com.fasterxml.jackson.core:jackson-core:$jackson")
         library("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:$jackson")

         val testcontainers = "1.19.8"
         library("testcontainers-postgresql", "org.testcontainers:postgresql:$testcontainers")
         library("testcontainers-mysql", "org.testcontainers:mysql:$testcontainers")
         library("testcontainers-localstack", "org.testcontainers:localstack:$testcontainers")
         library("testcontainers-vault", "org.testcontainers:vault:$testcontainers")

         library("azure-identity", "com.azure:azure-identity:1.10.3")
         library("azure-security-keyvault-secrets", "com.azure:azure-security-keyvault-secrets:4.7.0")

         library("consul-client", "com.orbitz.consul:consul-client:1.5.3")
      }
   }
}
