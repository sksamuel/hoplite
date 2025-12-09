plugins {
   id("com.vanniktech.maven.publish")
}

group = "com.sksamuel.hoplite"
version = Ci.version

mavenPublishing {
   publishToMavenCentral(automaticRelease = true)
   signAllPublications()
   pom {
      name.set("hoplite")
      description.set("Configuration for Kotlin")
      url.set("https://www.github.com/sksamuel/hoplite")

      scm {
         connection.set("scm:git:https://www.github.com/sksamuel/hoplite")
         developerConnection.set("scm:git:https://github.com/sksamuel")
         url.set("https://www.github.com/sksamuel/hoplite")
      }

      licenses {
         license {
            name.set("The Apache 2.0 License")
            url.set("https://opensource.org/licenses/Apache-2.0")
         }
      }

      developers {
         developer {
            id.set("sksamuel")
            name.set("Stephen Samuel")
            email.set("sam@sksamuel.com")
         }
      }
   }
}
