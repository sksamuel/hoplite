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

include("hoplite-core")
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
include("hoplite-toml")
include("hoplite-vavr")
include("hoplite-watch")
include("hoplite-watch-consul")
include("hoplite-yaml")
