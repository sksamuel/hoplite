dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Typesafe.config)
}

apply("../publish.gradle.kts")
