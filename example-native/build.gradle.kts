plugins {
   kotlin("jvm")
   application
   id("org.graalvm.buildtools.native")
}

repositories {
    mavenCentral()
}

dependencies {
   implementation("com.sksamuel.hoplite:hoplite-core:_")
   implementation("com.sksamuel.hoplite:hoplite-yaml:_")
}

application {
   mainClass.set("com.sksamuel.hoplite.example.MainKt")

   applicationDefaultJvmArgs = if (System.getProperty("native.image.agent") != null) {
      listOf("-agentlib:native-image-agent=config-output-dir=META-INF/native-image/generated")
   } else emptyList()
}

tasks.getByName("run") {
   outputs.upToDateWhen { false }
}

nativeBuild {
   imageName.set("example")
   mainClass.set("com.sksamuel.hoplite.example.MainKt")

   buildArgs.addAll(
      "--no-fallback",
      "--enable-all-security-services",
      "--report-unsupported-elements-at-runtime",
      "--install-exit-handlers",
      "--allow-incomplete-classpath",
      """--initialize-at-build-time=
        |kotlin""".trimMargin().replace(System.lineSeparator(), ""),
      "-J--add-exports=java.management/sun.management=ALL-UNNAMED",
      "-H:+ReportUnsupportedElementsAtRuntime",
      "-H:+ReportExceptionStackTraces",
      "-H:ReflectionConfigurationFiles=${projectDir}/META-INF/native-image/generated/reflect-config.json",
      """-H:ResourceConfigurationFiles=
        |${projectDir}/META-INF/native-image/kotlin-resource.json,
        |${projectDir}/META-INF/native-image/generated/resource-config.json""".trimMargin().replace(System.lineSeparator(), "")
   )
}
