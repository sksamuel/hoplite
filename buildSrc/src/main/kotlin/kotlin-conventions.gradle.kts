import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
   `java-library`
   kotlin("jvm")
}

java {
   sourceCompatibility = JavaVersion.VERSION_1_8
   targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
   jvmToolchain(11)
   compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
      apiVersion.set(KotlinVersion.KOTLIN_2_2)
      languageVersion.set(KotlinVersion.KOTLIN_2_2)
   }
}

tasks.compileJava {
   options.release = 8
}

tasks.compileTestKotlin {
   compilerOptions.jvmTarget = JvmTarget.JVM_11
}

tasks.compileTestJava {
   options.release = 11
}

dependencies {
   testImplementation("io.kotest:kotest-runner-junit5:6.0.7")
   testImplementation("io.kotest:kotest-assertions-core:6.0.7")
   testImplementation("io.kotest:kotest-extensions-testcontainers:6.0.7")
}

tasks.withType<Test> {
   useJUnitPlatform()
   filter {
      isFailOnNoMatchingTests = false
   }
   testLogging {
      showExceptions = true
      showStandardStreams = true
      events = setOf(
         TestLogEvent.FAILED,
         TestLogEvent.PASSED
      )
      exceptionFormat = TestExceptionFormat.FULL
   }
}
