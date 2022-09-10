dependencies {
   api(projects.hopliteCore)
   api(projects.hopliteWatch)
   implementation(libs.consul.client)

   testImplementation(libs.embedded.consul)
   testImplementation(projects.hopliteConsul)
   testImplementation(projects.hopliteJson)
   testImplementation(projects.hopliteYaml)
}

apply("../publish.gradle.kts")
