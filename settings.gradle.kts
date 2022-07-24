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

plugins {
   id("de.fayard.refreshVersions") version "0.40.2"
}

refreshVersions {
}

include("hoplite-core")
include("hoplite-azure")
include("hoplite-aws")
include("hoplite-aws2")
include("hoplite-arrow")
include("hoplite-consul")
include("hoplite-cronutils")
include("hoplite-datetime")
include("hoplite-hdfs")
include("hoplite-hikaricp")
include("hoplite-hocon")
include("hoplite-javax")
include("hoplite-json")
include("hoplite-micrometer-datadog")
include("hoplite-micrometer-prometheus")
include("hoplite-micrometer-statsd")
include("hoplite-toml")
include("hoplite-vault")
include("hoplite-vavr")
include("hoplite-watch")
include("hoplite-watch-consul")
include("hoplite-yaml")
