dependencies {
   api(projects.hopliteCore)

   implementation(libs.tomlj) {
      artifact {
         classifier = "all"
      }
   }
}

apply("../publish.gradle.kts")
