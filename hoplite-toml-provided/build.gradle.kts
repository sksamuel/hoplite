dependencies {
   api(projects.hopliteCore)
   compileOnly(libs.tomlj)
}

apply("../publish.gradle.kts")
