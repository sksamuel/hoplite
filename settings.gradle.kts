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

         val micrometer = "1.11.2"
         library("micrometer-core", "io.micrometer:micrometer-core:$micrometer")
         library("micrometer-datadog", "io.micrometer:micrometer-registry-datadog:$micrometer")

         val coroutines = "1.6.4"
         library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
         library("coroutines-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines")

         library("typesafe-config", "com.typesafe:config:1.4.2")

         library("arrow-core", "io.arrow-kt:arrow-core:1.1.3")

         library("spring-vault-core", "org.springframework.vault:spring-vault-core:2.3.2")

         library("vavr-kotlin", "io.vavr:vavr-kotlin:0.10.2")

         val aws1 = "1.12.523"
         library("aws-java-sdk-secretsmanager", "com.amazonaws:aws-java-sdk-secretsmanager:$aws1")

         library("testcontainers-postgresql", "org.testcontainers:postgresql:1.18.3")
         library("testcontainers-mysql", "org.testcontainers:mysql:1.18.3")
         library("testcontainers-localstack", "org.testcontainers:localstack:1.18.3")
         library("testcontainers-vault", "org.testcontainers:vault:1.18.3")

         library("azure-identity", "com.azure:azure-identity:1.9.1")
         library("azure-security-keyvault-secrets", "com.azure:azure-security-keyvault-secrets:4.6.4")

         library("consul-client", "com.orbitz.consul:consul-client:1.5.3")
      }
   }
}
