plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.cron.utils)
}
