dependencies {
   api(projects.hopliteCore)
   api(projects.hopliteWatch)
   implementation(Libs.Orbitz.consul)

   testImplementation(Libs.EmbeddedConsul.consul)
   testImplementation(projects.hopliteConsul)
   testImplementation(projects.hopliteJson)
   testImplementation(projects.hopliteYaml)
}

apply("../publish.gradle.kts")
