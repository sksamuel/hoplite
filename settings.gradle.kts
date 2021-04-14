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
include("hoplite-arrow")
include("hoplite-cronutils")
include("hoplite-datetime")
include("hoplite-hdfs")
include("hoplite-hikaricp")
include("hoplite-hocon")
include("hoplite-javax")
include("hoplite-json")
include("hoplite-toml")
include("hoplite-yaml")
include("hoplite-vavr")
